/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AdvisoryLockError
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.fdresource.nio.NioFileChannel
import at.released.weh.filesystem.fdresource.nio.NioFileStat
import at.released.weh.filesystem.fdresource.nio.nioAddAdvisoryLock
import at.released.weh.filesystem.fdresource.nio.nioRead
import at.released.weh.filesystem.fdresource.nio.nioRemoveAdvisoryLock
import at.released.weh.filesystem.fdresource.nio.nioSetPosixFilePermissions
import at.released.weh.filesystem.fdresource.nio.nioSetPosixUserGroup
import at.released.weh.filesystem.fdresource.nio.nioSetTimestamp
import at.released.weh.filesystem.fdresource.nio.nioWrite
import at.released.weh.filesystem.fdresource.nio.setPosition
import at.released.weh.filesystem.fdresource.nio.sync
import at.released.weh.filesystem.fdresource.nio.truncate
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.nio.NioFileSystemState
import at.released.weh.filesystem.nio.NioSeekFd.Companion.toSeekError
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat
import kotlinx.io.IOException
import java.nio.channels.FileChannel
import java.nio.channels.FileLock
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.nio.file.Path as NioPath

internal class NioFileFdResource(
    val fileSystem: NioFileSystemState,
    path: NioPath,
    channel: FileChannel,
) : FdResource {
    val lock: Lock = ReentrantLock()
    val fileLocks: MutableMap<FileLockKey, FileLock> = mutableMapOf()
    val channel = NioFileChannel(path, channel)

    override fun stat(): Either<StatError, StructStat> {
        return NioFileStat.getStat(channel.path, true)
    }

    override fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long> {
        return channel.setPosition(fileDelta, whence)
            .mapLeft { error -> error.toSeekError() }
    }

    override fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong> {
        return channel.nioRead(iovecs, strategy)
    }

    override fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong> {
        return channel.nioWrite(cIovecs, strategy)
    }

    override fun sync(syncMetadata: Boolean): Either<SyncError, Unit> = channel.sync(syncMetadata)

    override fun truncate(length: Long): Either<TruncateError, Unit> = channel.truncate(length)

    override fun chmod(mode: Int): Either<ChmodError, Unit> {
        return nioSetPosixFilePermissions(channel.path, mode)
    }

    override fun chown(owner: Int, group: Int): Either<ChownError, Unit> {
        return nioSetPosixUserGroup(fileSystem.javaFs, channel.path, owner, group)
    }

    override fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit> {
        return nioSetTimestamp(channel.path, false, atimeNanoseconds, mtimeNanoseconds)
    }

    override fun close(): Either<CloseError, Unit> {
        return try {
            channel.channel.close()
            Unit.right()
        } catch (ioe: IOException) {
            IoError("close() error: $ioe").left()
        }
    }

    override fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> = nioAddAdvisoryLock(flock)

    override fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit> =
        nioRemoveAdvisoryLock(flock)
}
