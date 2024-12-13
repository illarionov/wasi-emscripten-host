/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.windows.nativefunc.readwrite.writeDoNotChangePosition
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileStandardInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileAllocationSize
import platform.windows.HANDLE

internal fun HANDLE.fallocate(offset: Long, length: Long): Either<FallocateError, Unit> = either {
    if (offset < 0) {
        raise(InvalidArgument("Incorrect offset $offset"))
    }
    if (length <= 0) {
        raise(InvalidArgument("Incorrect length $length"))
    }

    val standardInfo = getFileStandardInfo().mapLeft(StatError::toFallocateError).bind()

    val fileSize = standardInfo.endOfFile

    val newFileSize = (offset + length).coerceAtLeast(fileSize)
    if (newFileSize > fileSize) {
        if (newFileSize > standardInfo.allocationSize) {
            setFileAllocationSize(newFileSize).bind()
        }
        // write last byte to change file size without changing file position
        writeDoNotChangePosition(
            (newFileSize - 1).toULong(),
            listOf(FileSystemByteBuffer(ByteArray(1))),
        )
    }
}

private fun StatError.toFallocateError(): FallocateError = when (this) {
    is AccessDenied -> IoError(this.message)
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is IoError -> this
    is NameTooLong -> InvalidArgument(this.message)
    is NoEntry -> InvalidArgument(this.message)
    is NotCapable -> InvalidArgument(this.message)
    is NotDirectory -> InvalidArgument(this.message)
    is TooManySymbolicLinks -> InvalidArgument(this.message)
}
