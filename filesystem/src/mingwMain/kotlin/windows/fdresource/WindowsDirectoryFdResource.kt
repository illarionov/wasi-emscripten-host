/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.fdresource

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FadviseError
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.PathIsDirectory
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
import at.released.weh.filesystem.posix.fdresource.PosixFdResource
import at.released.weh.filesystem.posix.fdresource.PosixFdResource.FdResourceType
import at.released.weh.filesystem.posix.fdresource.PosixFdResource.FdResourceType.DIRECTORY
import at.released.weh.filesystem.preopened.VirtualPath
import at.released.weh.filesystem.windows.nativefunc.windowsCloseHandle
import platform.windows.HANDLE

internal class WindowsDirectoryFdResource(
    channel: WindowsDirectoryChannel,
) : PosixFdResource, FdResource {
    internal val channel = channel.copy()
    override val fdResourceType: FdResourceType = DIRECTORY

    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> {
        TODO()
        // return linuxFdAttributes(nativeFd, rights)
    }

    override fun stat(): Either<StatError, StructStat> {
        TODO()
        // return linuxStatFd(nativeFd.linuxFd)
    }

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        return BadFileDescriptor("Can not seek on a directory").left()
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        return BadFileDescriptor("Can not read on a directory").left()
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        return BadFileDescriptor("Can not write on a directory").left()
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> {
        return BadFileDescriptor("Can not sync on a directory").left()
    }

    override fun fadvise(offset: Long, length: Long, advice: Advice): Either<FadviseError, Unit> {
        return BadFileDescriptor("Can not set advice on a directory").left()
    }

    override fun fallocate(offset: Long, length: Long): Either<FallocateError, Unit> {
        return PathIsDirectory("Can not allocate on a directory").left()
    }

    override fun truncate(length: Long): Either<TruncateError, Unit> {
        return BadFileDescriptor("Can not truncate on a directory").left()
    }

    override fun chmod(mode: Int): Either<ChmodError, Unit> {
        return NotSupported("No supported by host operating system").left()
    }

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> {
        return NotSupported("No supported by host operating system").left()
    }

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        TODO()
        // return linuxSetTimestamp(nativeFd, atimeNanoseconds, mtimeNanoseconds)
    }

    override fun setFdFlags(flags: Fdflags): Either<SetFdFlagsError, Unit> {
        return BadFileDescriptor("Can not change flags of the opened directory").left()
    }

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return BadFileDescriptor("Can not add advisory lock on a directory").left()
    }

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return BadFileDescriptor("Can not add remove lock on a directory").left()
    }

    override fun close(): Either<CloseError, Unit> = windowsCloseHandle(channel.handle)

    internal data class WindowsDirectoryChannel(
        val handle: HANDLE,
        val isPreopened: Boolean = false,
        val rights: FdRightsBlock,
        val virtualPath: VirtualPath,
    )
}
