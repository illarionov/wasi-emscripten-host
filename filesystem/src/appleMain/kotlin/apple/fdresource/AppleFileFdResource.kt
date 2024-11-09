/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.fdresource

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleStatFd
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

// TODO: merge with Linux
internal class AppleFileFdResource(
    channel: NativeFileChannel,
) : PosixFdResource, FdResource {
    private val channel = channel.copy()
    override val fdResourceType: FdResourceType = FdResourceType.FILE

    override fun fdAttributes(): Either<FdAttributesError, FdAttributesResult> = TODO()

    override fun stat(): Either<StatError, StructStat> = appleStatFd(channel.fd.fd)

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        TODO()
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        TODO()
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        TODO()
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> = TODO()

    override fun fadvise(offset: Long, length: Long, advice: Advice): Either<FadviseError, Unit> {
        TODO()
    }

    override fun fallocate(offset: Long, length: Long): Either<FallocateError, Unit> {
        TODO()
    }

    override fun truncate(length: Long): Either<TruncateError, Unit> = TODO()

    override fun chmod(mode: Int): Either<ChmodError, Unit> = TODO()

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> = TODO()

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        TODO()
    }

    override fun setFdFlags(flags: Fdflags): Either<SetFdFlagsError, Unit> {
        TODO()
    }

    override fun close(): Either<CloseError, Unit> = TODO()

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> =
        TODO()

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        TODO()
    }

    internal data class NativeFileChannel(
        val fd: NativeFileFd,
        val rights: FdRightsBlock,
    )
}
