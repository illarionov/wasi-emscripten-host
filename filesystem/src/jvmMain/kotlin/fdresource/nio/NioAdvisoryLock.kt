/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoLock
import at.released.weh.filesystem.fdresource.FileLockKey
import at.released.weh.filesystem.fdresource.NioFileFdResource
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.lock.AdvisorylockLockType
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileLock
import java.nio.channels.NonReadableChannelException
import java.nio.channels.NonWritableChannelException
import java.nio.channels.OverlappingFileLockException
import kotlin.concurrent.withLock

internal fun NioFileFdResource.nioAddAdvisoryLock(
    flock: Advisorylock,
): Either<AdvisoryLockError, Unit> {
    val position = channel.resolveWhencePosition(flock.start, flock.whence)
        .getOrElse {
            return it.toAdvisoryLockError().left()
        }

    // Unlock overlapping locks
    nioRemoveAdvisoryLock(flock)
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
    }.mapLeft(::toAdvisoryLockError)
        .flatMap { fileLock ->
            fileLock?.right() ?: Again("Lock held").left()
        }

    return lockResult.onRight { fileLock: FileLock ->
        val fileLockKey = FileLockKey(position, flock.length)
        val oldLock = lock.withLock {
            fileLocks.put(fileLockKey, fileLock)
        }
        try {
            oldLock?.release()
        } catch (ignore: IOException) {
            // ignore
        }
    }.map { }
}

internal fun NioFileFdResource.nioRemoveAdvisoryLock(
    flock: Advisorylock,
): Either<AdvisoryLockError, Unit> {
    val position = channel.resolveWhencePosition(flock.start, flock.whence)
        .getOrElse {
            return it.toAdvisoryLockError().left()
        }

    val locksToRelease: List<FileLock> = lock.withLock {
        val locks: MutableList<FileLock> = mutableListOf()
        val iterator = fileLocks.iterator()
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

private fun ChannelPositionError.toAdvisoryLockError(): AdvisoryLockError = when (this) {
    is ChannelPositionError.ClosedChannel -> BadFileDescriptor(message)
    is ChannelPositionError.InvalidArgument -> InvalidArgument(message)
    is ChannelPositionError.IoError -> IoError(message)
}

private fun toAdvisoryLockError(error: Throwable): AdvisoryLockError {
    val advisoryLockError = when (error) {
        is IllegalArgumentException -> InvalidArgument("Parameter validation failed: ${error.message}")
        is ClosedChannelException -> BadFileDescriptor("Channel already closed (${error.message})")
        is OverlappingFileLockException -> BadFileDescriptor("Overlapping lock: ${error.message}")
        is NonReadableChannelException -> BadFileDescriptor("Channel not open for reading: ${error.message}")
        is NonWritableChannelException -> BadFileDescriptor("Channel not open for writing: ${error.message}")
        is IOException -> NoLock("IO exception: ${error.message}")
        else -> InvalidArgument("Unexpected error: ${error.message}")
    }
    return advisoryLockError
}
