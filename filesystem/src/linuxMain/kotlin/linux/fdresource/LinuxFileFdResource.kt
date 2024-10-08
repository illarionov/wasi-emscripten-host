/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.fdresource

import arrow.core.Either
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.linux.native.linuxAddAdvisoryLockFd
import at.released.weh.filesystem.linux.native.linuxChmodFd
import at.released.weh.filesystem.linux.native.linuxChownFd
import at.released.weh.filesystem.linux.native.linuxRemoveAdvisoryLock
import at.released.weh.filesystem.linux.native.linuxSeek
import at.released.weh.filesystem.linux.native.linuxSetTimestamp
import at.released.weh.filesystem.linux.native.linuxStatFd
import at.released.weh.filesystem.linux.native.linuxSync
import at.released.weh.filesystem.linux.native.linuxTruncate
import at.released.weh.filesystem.linux.native.posixClose
import at.released.weh.filesystem.linux.native.posixRead
import at.released.weh.filesystem.linux.native.posixWrite
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.NativeFd
import at.released.weh.filesystem.posix.fdresource.PosixFileFdResource

internal class LinuxFileFdResource(
    override val nativeFd: NativeFd,
) : PosixFileFdResource, FdResource {
    override fun stat(): Either<StatError, StructStat> = linuxStatFd(nativeFd)

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        return linuxSeek(nativeFd, fileDelta, whence)
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        return posixRead(nativeFd, iovecs, strategy)
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        return posixWrite(nativeFd, cIovecs, strategy)
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> = linuxSync(nativeFd, syncMetadata)

    override fun truncate(length: Long): Either<TruncateError, Unit> = linuxTruncate(nativeFd, length)

    override fun chmod(mode: Int): Either<ChmodError, Unit> = linuxChmodFd(nativeFd, mode)

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> = linuxChownFd(nativeFd, owner, group)

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return linuxSetTimestamp(nativeFd, atimeNanoseconds, mtimeNanoseconds)
    }

    override fun close(): Either<CloseError, Unit> = posixClose(nativeFd)

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> =
        linuxAddAdvisoryLockFd(nativeFd, flock)

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> {
        return linuxRemoveAdvisoryLock(nativeFd, flock)
    }
}
