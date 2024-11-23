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
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.fadvise.Advice
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.windows.nativefunc.fallocate
import at.released.weh.filesystem.windows.nativefunc.readwrite.read
import at.released.weh.filesystem.windows.nativefunc.readwrite.write
import at.released.weh.filesystem.windows.nativefunc.stat.windowsStatFd
import at.released.weh.filesystem.windows.nativefunc.truncate
import at.released.weh.filesystem.windows.nativefunc.windowsGetFdAttributes
import at.released.weh.filesystem.windows.win32api.ext.fromNanoseconds
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileBasicInfo
import at.released.weh.filesystem.windows.win32api.flushFileBuffers
import at.released.weh.filesystem.windows.win32api.setFilePointer
import at.released.weh.filesystem.windows.win32api.windowsCloseHandle
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import platform.windows.HANDLE

internal class WindowsFileFdResource(
    channel: WindowsFileChannel,
) : FdResource {
    private val channel = WindowsFileChannel(
        channel.handle,
        channel.isInAppendMode,
        channel.rights,
        channel.fdresourceLock,
    )

    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> {
        return windowsGetFdAttributes(channel.handle, channel.isInAppendMode, channel.rights)
    }

    override fun stat(): Either<StatError, StructStat> {
        return windowsStatFd(channel.handle)
    }

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        return channel.handle.setFilePointer(fileDelta, whence)
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        return channel.handle.read(iovecs, strategy)
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        return channel.handle.write(cIovecs, strategy, channel.isInAppendMode)
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> {
        return channel.handle.flushFileBuffers()
    }

    override fun fadvise(offset: Long, length: Long, advice: Advice): Either<FadviseError, Unit> {
        // Do nothing. File flags cannot be changed for open files on Windows
        return Unit.right()
    }

    override fun fallocate(offset: Long, length: Long): Either<FallocateError, Unit> {
        return channel.handle.fallocate(offset, length)
    }

    override fun truncate(length: Long): Either<TruncateError, Unit> {
        return channel.handle.truncate(length)
    }

    override fun chmod(mode: Int): Either<ChmodError, Unit> {
        return NotSupported("Not supported by file system").left()
    }

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> {
        return NotSupported("Not supported by file system").left()
    }

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return channel.handle.setFileBasicInfo(
            creationTime = null,
            lastAccessTime = atimeNanoseconds?.let(StructTimespec::fromNanoseconds),
            lastWriteTime = mtimeNanoseconds?.let(StructTimespec::fromNanoseconds),
            changeTime = mtimeNanoseconds?.let(StructTimespec::fromNanoseconds),
            fileAttributes = null,
        )
    }

    override fun setFdFlags(flags: Fdflags): Either<SetFdFlagsError, Unit> {
        // Only APPEND flag is changeable. Should we use ReOpenFile to set other flags?
        channel.updateIsInAppendMode { _ -> flags and FD_APPEND == FD_APPEND }
        return Unit.right()
    }

    override fun close(): Either<CloseError, Unit> = windowsCloseHandle(channel.handle)

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return NotSupported("Not yet implemented").left()
    }

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return NotSupported("Not yet implemented").left()
    }

    internal class WindowsFileChannel(
        val handle: HANDLE,
        isInAppendMode: Boolean = false,
        val rights: FdRightsBlock,
        internal val fdresourceLock: ReentrantLock = reentrantLock(),
    ) {
        private var _isInAppendMode: Boolean = isInAppendMode
        public val isInAppendMode: Boolean
            get() = fdresourceLock.withLock { _isInAppendMode }

        internal inline fun updateIsInAppendMode(valueFactory: (Boolean) -> Boolean): Unit = fdresourceLock.withLock {
            _isInAppendMode = valueFactory(_isInAppendMode)
        }
    }
}
