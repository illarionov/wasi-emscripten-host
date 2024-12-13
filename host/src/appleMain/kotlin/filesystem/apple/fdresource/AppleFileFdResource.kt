/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.fdresource

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.apple.nativefunc.appleChmodFd
import at.released.weh.filesystem.apple.nativefunc.appleChownFd
import at.released.weh.filesystem.apple.nativefunc.appleFallocate
import at.released.weh.filesystem.apple.nativefunc.appleFdAttributes
import at.released.weh.filesystem.apple.nativefunc.appleRead
import at.released.weh.filesystem.apple.nativefunc.appleSetFdflags
import at.released.weh.filesystem.apple.nativefunc.appleSetTimestamp
import at.released.weh.filesystem.apple.nativefunc.appleStatFd
import at.released.weh.filesystem.apple.nativefunc.appleSync
import at.released.weh.filesystem.apple.nativefunc.appleTruncate
import at.released.weh.filesystem.apple.nativefunc.appleWrite
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FadviseError
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.FdAttributesError
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
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.fdresource.PosixFdResource
import at.released.weh.filesystem.posix.fdresource.PosixFdResource.FdResourceType
import at.released.weh.filesystem.posix.nativefunc.posixAddAdvisoryLockFd
import at.released.weh.filesystem.posix.nativefunc.posixClose
import at.released.weh.filesystem.posix.nativefunc.posixRemoveAdvisoryLock
import at.released.weh.filesystem.posix.nativefunc.posixSeek

internal class AppleFileFdResource(
    channel: NativeFileChannel,
) : PosixFdResource, FdResource {
    private val channel = channel.copy()
    override val fdResourceType: FdResourceType = FdResourceType.FILE

    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> = appleFdAttributes(channel)

    override fun stat(): Either<StatError, StructStat> = appleStatFd(channel.fd.fd)

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        return posixSeek(channel.fd, fileDelta, whence)
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        return appleRead(channel, iovecs, strategy)
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        return appleWrite(channel, cIovecs, strategy)
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> = appleSync(channel.fd, syncMetadata)

    override fun fadvise(offset: Long, length: Long, advice: Advice): Either<FadviseError, Unit> {
        // Not available on macOS
        return Unit.right()
    }

    override fun fallocate(offset: Long, length: Long): Either<FallocateError, Unit> {
        return appleFallocate(channel.fd, offset, length)
    }

    override fun truncate(length: Long): Either<TruncateError, Unit> = appleTruncate(channel.fd, length)

    override fun chmod(mode: Int): Either<ChmodError, Unit> = appleChmodFd(channel.fd, mode)

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> = appleChownFd(channel.fd, owner, group)

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return appleSetTimestamp(channel.fd, atimeNanoseconds, mtimeNanoseconds)
    }

    override fun setFdFlags(flags: Fdflags): Either<SetFdFlagsError, Unit> = appleSetFdflags(channel, flags)

    override fun close(): Either<CloseError, Unit> = posixClose(channel.fd)

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> =
        posixAddAdvisoryLockFd(channel.fd, flock)

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> =
        posixRemoveAdvisoryLock(channel.fd, flock)

    internal data class NativeFileChannel(
        val fd: NativeFileFd,
        val rights: FdRightsBlock,
    )
}
