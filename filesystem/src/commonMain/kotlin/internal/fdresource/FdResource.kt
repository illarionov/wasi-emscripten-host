/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.fdresource

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
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.lock.Advisorylock
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.StructStat

internal interface FdResource {
    fun stat(): Either<StatError, StructStat>

    fun seek(fileDelta: Long, whence: Whence): Either<SeekError, Long>

    fun read(iovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<ReadError, ULong>

    fun write(cIovecs: List<FileSystemByteBuffer>, strategy: ReadWriteStrategy): Either<WriteError, ULong>

    fun sync(syncMetadata: Boolean): Either<SyncError, Unit>

    fun truncate(length: Long): Either<TruncateError, Unit>

    fun chmod(@FileMode mode: Int): Either<ChmodError, Unit>

    fun chown(owner: Int, group: Int): Either<ChownError, Unit>

    fun setTimestamp(atimeNanoseconds: Long?, mtimeNanoseconds: Long?): Either<SetTimestampError, Unit>

    fun addAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit>

    fun removeAdvisoryLock(flock: Advisorylock): Either<AdvisoryLockError, Unit>

    fun close(): Either<CloseError, Unit>
}
