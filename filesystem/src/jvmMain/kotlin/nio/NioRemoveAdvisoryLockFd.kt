/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.fd.NioFileHandle
import at.released.weh.filesystem.fd.resolveWhencePosition
import at.released.weh.filesystem.internal.ChannelPositionError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.lock.RemoveAdvisoryLockFd
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileLock
import kotlin.concurrent.withLock
import at.released.weh.filesystem.error.InvalidArgument as BaseInvalidArgument

internal class NioRemoveAdvisoryLockFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<RemoveAdvisoryLockFd, AdvisoryLockError, Unit> {
    override fun invoke(input: RemoveAdvisoryLockFd): Either<AdvisoryLockError, Unit> {
        val channel = fsState.fileDescriptors.get(input.fd)
            ?: return BadFileDescriptor("File descriptor ${input.fd} is not opened").left()

        return removeAdvisoryLock(channel, input.flock)
    }
}

internal fun removeAdvisoryLock(
    channel: NioFileHandle,
    flock: Advisorylock,
): Either<AdvisoryLockError, Unit> {
    val position = channel.resolveWhencePosition(flock.start, flock.whence)
        .getOrElse {
            return it.toAdvisoryLockError().left()
        }

    val locksToRelease: List<FileLock> = channel.lock.withLock {
        val locks: MutableList<FileLock> = mutableListOf()
        val iterator = channel.fileLocks.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            val lock = item.value
            if (lock.overlaps(position, flock.length)) {
                iterator.remove()
                locks.add(lock)
            }
        }
        locks
    }

    val releaseErrors: List<Either<AdvisoryLockError, Unit>> = locksToRelease.map { fileLock ->
        Either.catch {
            fileLock.release()
        }.mapLeft {
            when (it) {
                is ClosedChannelException -> BadFileDescriptor("Channel `$fileLock` already closed")
                is IOException -> IoError("I/O error (${it.message})")
                else -> throw IllegalStateException("Unexpected error", it)
            }
        }
    }

    return releaseErrors.firstOrNull { it.isLeft() } ?: Unit.right()
}

internal fun ChannelPositionError.toAdvisoryLockError(): AdvisoryLockError = when (this) {
    is ChannelPositionError.ClosedChannel -> BadFileDescriptor(message)
    is ChannelPositionError.InvalidArgument -> BaseInvalidArgument(message)
    is ChannelPositionError.IoError -> IoError(message)
}
