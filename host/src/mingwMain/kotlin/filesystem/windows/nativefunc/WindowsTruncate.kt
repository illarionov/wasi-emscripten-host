/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.Overflow
import at.released.weh.filesystem.error.Pipe
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.model.Whence.SET
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.windows.nativefunc.readwrite.writeDoNotChangePosition
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileStandardInfo
import at.released.weh.filesystem.windows.win32api.getFilePointer
import at.released.weh.filesystem.windows.win32api.setEndOfFile
import at.released.weh.filesystem.windows.win32api.setFilePointer
import platform.windows.HANDLE

internal fun HANDLE.truncate(length: Long): Either<TruncateError, Unit> = either {
    val originalSize = getFileStandardInfo().mapLeft(StatError::toTruncateError).bind().endOfFile
    if (originalSize == length) {
        return Unit.right()
    }

    val originalFilePosition = getFilePointer().mapLeft(SeekError::toTruncateError).bind()
    val newFileSizePosition = if (originalFilePosition != length) {
        setFilePointer(length, SET).mapLeft(SeekError::toTruncateError).bind()
    } else {
        originalFilePosition
    }

    val setEndOfFileResult: Either<TruncateError, Unit> = this@truncate.setEndOfFile()

    if (originalSize < length) {
        // write last zero byte to to ensure that all the new space is read as zeros
        writeDoNotChangePosition(
            (length - 1).toULong(),
            listOf(FileSystemByteBuffer(ByteArray(1))),
        )
    }

    // Restore file position
    if (originalFilePosition != newFileSizePosition) {
        setFilePointer(originalFilePosition, SET).mapLeft(SeekError::toTruncateError).bind()
    }
    return setEndOfFileResult
}

private fun SeekError.toTruncateError(): TruncateError = when (this) {
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is Nxio -> IoError(this.message)
    is Overflow -> InvalidArgument(this.message)
    is Pipe -> IoError(this.message)
}

private fun StatError.toTruncateError(): TruncateError = when (this) {
    is AccessDenied -> this
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is IoError -> this
    is NameTooLong -> this
    is NoEntry -> this
    is NotCapable -> AccessDenied(this.message)
    is NotDirectory -> this
    is TooManySymbolicLinks -> this
    is Nfile -> IoError(message)
}
