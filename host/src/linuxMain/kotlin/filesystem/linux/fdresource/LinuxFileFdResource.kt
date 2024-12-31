/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.fdresource

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FadviseError
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.error.SetFdFlagsError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.linux.native.linuxChmodFd
import at.released.weh.filesystem.linux.native.linuxChownFd
import at.released.weh.filesystem.linux.native.linuxFadvise
import at.released.weh.filesystem.linux.native.linuxFdAttributes
import at.released.weh.filesystem.linux.native.linuxRead
import at.released.weh.filesystem.linux.native.linuxSetFdflags
import at.released.weh.filesystem.linux.native.linuxSetTimestamp
import at.released.weh.filesystem.linux.native.linuxStatFd
import at.released.weh.filesystem.linux.native.linuxSync
import at.released.weh.filesystem.linux.native.linuxTruncate
import at.released.weh.filesystem.linux.native.linuxWrite
import at.released.weh.filesystem.linux.native.posixFallocate
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.fadvise.Advice
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.poll.Event.FileDescriptorEvent
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.nativefunc.posixAddAdvisoryLockFd
import at.released.weh.filesystem.posix.nativefunc.posixClose
import at.released.weh.filesystem.posix.nativefunc.posixRemoveAdvisoryLock
import at.released.weh.filesystem.posix.nativefunc.posixSeek

internal class LinuxFileFdResource(
    channel: NativeFileChannel,
) : FdResource {
    private val channel = channel.copy()

    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> = linuxFdAttributes(channel)

    override fun stat(): Either<StatError, StructStat> = linuxStatFd(channel.fd.fd)

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        return posixSeek(channel.fd, fileDelta, whence)
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        return linuxRead(channel, iovecs, strategy)
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        return linuxWrite(channel, cIovecs, strategy)
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> = linuxSync(channel.fd, syncMetadata)

    override fun fadvise(offset: Long, length: Long, advice: Advice): Either<FadviseError, Unit> {
        return linuxFadvise(channel.fd, offset, length, advice)
    }

    override fun fallocate(offset: Long, length: Long): Either<FallocateError, Unit> {
        return posixFallocate(channel.fd, offset, length)
    }

    override fun truncate(length: Long): Either<TruncateError, Unit> = linuxTruncate(channel.fd, length)

    override fun chmod(mode: Int): Either<ChmodError, Unit> = linuxChmodFd(channel.fd, mode)

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> = linuxChownFd(channel.fd, owner, group)

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return linuxSetTimestamp(channel.fd, atimeNanoseconds, mtimeNanoseconds)
    }

    override fun setFdFlags(flags: Fdflags): Either<SetFdFlagsError, Unit> {
        return linuxSetFdflags(channel, flags)
    }

    override fun close(): Either<CloseError, Unit> = posixClose(channel.fd)

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> =
        posixAddAdvisoryLockFd(channel.fd, flock)

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return posixRemoveAdvisoryLock(channel.fd, flock)
    }

    override fun pollNonblocking(
        subscription: FileDescriptorSubscription,
    ): Either<NonblockingPollError, FileDescriptorEvent> {
        return FileDescriptorEvent(
            errno = FileSystemErrno.SUCCESS,
            userdata = subscription.userdata,
            fileDescriptor = subscription.fileDescriptor,
            type = subscription.type,
            bytesAvailable = 0,
            isHangup = false,
        ).right()
    }

    /**
     * We implement append mode without using the system's O_APPEND, as it restricts writing to
     * arbitrary positions in the file, which is necessary for implementing the `fd_write` WASI function.
     */
    internal data class NativeFileChannel(
        val fd: NativeFileFd,
        var isInAppendMode: Boolean = false,
        val rights: FdRightsBlock,
    )
}
