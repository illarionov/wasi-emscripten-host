/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoLock
import at.released.weh.filesystem.fd.FileLockKey
import at.released.weh.filesystem.fd.NioFileHandle
import at.released.weh.filesystem.fd.resolveWhencePosition
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.lock.AddAdvisoryLockFd
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.lock.AdvisorylockLockType
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileLock
import java.nio.channels.NonReadableChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.OverlappingFileLockException
import kotlin.concurrent.withLock

internal class NioAddAdvisoryLockFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<AddAdvisoryLockFd, AdvisoryLockError, Unit> {
    override fun invoke(input: AddAdvisoryLockFd): Either<AdvisoryLockError, Unit> = fsState.fsLock.withLock {
        val channel = fsState.fileDescriptors.get(input.fd)
            ?: return BadFileDescriptor("File descriptor ${input.fd} is not opened").left()

        return addAdvisorylock(channel, input.flock)
    }

    companion object {
        internal fun addAdvisorylock(
            channel: NioFileHandle,
            flock: Advisorylock,
        ): Either<AdvisoryLockError, Unit> {
            val position = channel.resolveWhencePosition(flock.start, flock.whence)
                .getOrElse {
                    return it.toAdvisoryLockError().left()
                }

            // Unlock overlapping locks
            removeAdvisoryLock(channel, flock)
                .onLeft {
                    return it.left()
                }

            // Lock new
            val isSharedLock = flock.type == AdvisorylockLockType.READ
            val lockResult: Either<AdvisoryLockError, FileLock> = Either.catch {
                channel.channel.tryLock(
                    position,
                    flock.length,
                    isSharedLock,
                )
            }.mapLeft { error ->
                error.toAdvisoryLockError()
            }.flatMap { fileLock ->
                fileLock?.right() ?: Again("Lock held").left()
            }

            return lockResult.onRight { lock ->
                val fileLockKey = FileLockKey(position, flock.length)
                val oldLock = channel.lock.withLock {
                    channel.fileLocks.put(fileLockKey, lock)
                }
                try {
                    oldLock?.release()
                } catch (ignore: IOException) {
                    // ignore
                }
            }.map { }
        }

        private fun Throwable.toAdvisoryLockError(): AdvisoryLockError {
            val advisoryLockError = when (this) {
                is IllegalArgumentException -> InvalidArgument("Parameter validation failed: $message")
                is ClosedChannelException -> BadFileDescriptor("Channel already closed ($message)")
                is OverlappingFileLockException -> BadFileDescriptor("Overlapping lock: $message")
                is NonReadableChannelException -> BadFileDescriptor("Channel not open for reading: $message")
                is NonWritableChannelException -> BadFileDescriptor("Channel not open for writing: $message")
                is IOException -> NoLock("IO exception: $message")
                else -> InvalidArgument("Unexpected error: $message")
            }
            return advisoryLockError
        }
    }
}
