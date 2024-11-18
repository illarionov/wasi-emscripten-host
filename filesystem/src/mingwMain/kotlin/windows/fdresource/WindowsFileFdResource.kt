/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.fdresource

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FadviseError
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.NotSupported
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
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.fadvise.Advice
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.fdresource.PosixFdResource.FdResourceType
import at.released.weh.filesystem.windows.nativefunc.windowsCloseHandle
import platform.windows.HANDLE

internal class WindowsFileFdResource(
    channel: WindowsFileChannel,
) : FdResource {
    private val channel = channel.copy()
    val fdResourceType: FdResourceType = FdResourceType.FILE

    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> {
        TODO()
        // return linuxFdAttributes(channel)
    }

    override fun stat(): Either<StatError, StructStat> {
        TODO()
        // return linuxStatFd(channel.fd.fd)
    }

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        TODO()
        // return posixSeek(channel.fd, fileDelta, whence)
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        TODO()
        // return linuxRead(channel, iovecs, strategy)
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        TODO()
        // return linuxWrite(channel, cIovecs, strategy)
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> {
        TODO()
        // return linuxSync(channel.fd, syncMetadata)
    }

    override fun fadvise(offset: Long, length: Long, advice: Advice): Either<FadviseError, Unit> {
        TODO()
        // return linuxFadvise(channel.fd, offset, length, advice)
    }

    override fun fallocate(offset: Long, length: Long): Either<FallocateError, Unit> {
        TODO()
        // return posixFallocate(channel.fd, offset, length)
    }

    override fun truncate(length: Long): Either<TruncateError, Unit> {
        TODO()
        // return linuxTruncate(channel.fd, length)
    }

    override fun chmod(mode: Int): Either<ChmodError, Unit> {
        TODO()
        // return linuxChmodFd(channel.fd, mode)
    }

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> {
        TODO()
        // return linuxChownFd(channel.fd, owner, group)
    }

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        TODO()
        // return linuxSetTimestamp(channel.fd, atimeNanoseconds, mtimeNanoseconds)
    }

    override fun setFdFlags(flags: Fdflags): Either<SetFdFlagsError, Unit> {
        TODO()
        // return linuxSetFdflags(channel, flags)
    }

    override fun close(): Either<CloseError, Unit> = windowsCloseHandle(channel.handle)

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return NotSupported("Not yet implemented").left()
        // return posixAddAdvisoryLockFd(channel.fd, flock)
    }

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return NotSupported("Not yet implemented").left()
        TODO()
        // return posixRemoveAdvisoryLock(channel.fd, flock)
    }

    /**
     * We implement append mode without using the system's O_APPEND, as it restricts writing to
     * arbitrary positions in the file, which is necessary for implementing the `fd_write` WASI function.
     */
    internal data class WindowsFileChannel(
        val handle: HANDLE,
        var isInAppendMode: Boolean = false,
        val rights: FdRightsBlock,
    )
}
