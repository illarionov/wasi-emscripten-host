/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.deviceiocontrol

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.toCommonError
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.ext.readChars
import at.released.weh.host.platform.windows.REPARSE_DATA_BUFFER
import at.released.weh.host.platform.windows.REPARSE_DATA_BUFFER_SYMLINK_PATH_BUFFER_OFFSET
import kotlinx.cinterop.alignOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.DeviceIoControl
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INSUFFICIENT_BUFFER
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.FSCTL_GET_REPARSE_POINT
import platform.windows.HANDLE
import platform.windows.IO_REPARSE_TAG_SYMLINK
import platform.windows.MAXIMUM_REPARSE_DATA_BUFFER_SIZE
import platform.windows.MAX_PATH
import platform.windows.SYMLINK_FLAG_RELATIVE
import platform.windows.WCHARVar

internal fun HANDLE.getReparsePoint(): Either<ReadLinkError, WindowsRealPath> {
    maxSizes().forEach { size ->
        memScoped {
            val outBuffer = alloc(size, alignOf<REPARSE_DATA_BUFFER>())
            val reparsePoint: REPARSE_DATA_BUFFER = outBuffer.reinterpret()
            val bytesReturned: DWORDVar = alloc()

            val result = DeviceIoControl(
                hDevice = this@getReparsePoint,
                dwIoControlCode = FSCTL_GET_REPARSE_POINT.toUInt(),
                lpInBuffer = null,
                nInBufferSize = 0U,
                lpOutBuffer = reparsePoint.ptr,
                nOutBufferSize = size.toUInt(),
                lpBytesReturned = bytesReturned.ptr,
                lpOverlapped = null,
            )
            if (result != 0) {
                return reparsePoint.readSymlink(bytesReturned.value.toInt())
            } else {
                val win32ErrorCode = Win32ErrorCode.getLast()
                if (win32ErrorCode.code.toInt() != ERROR_INSUFFICIENT_BUFFER) {
                    return win32ErrorCode.toReadLinkError().left()
                }
            }
        }
    }
    return NameTooLong("Target value too long").left()
}

private fun REPARSE_DATA_BUFFER.readSymlink(
    bufferSize: Int,
): Either<ReadLinkError, WindowsRealPath> = either {
    if (bufferSize < REPARSE_DATA_BUFFER_SYMLINK_PATH_BUFFER_OFFSET || bufferSize > MAXIMUM_REPARSE_DATA_BUFFER_SIZE) {
        raise(InvalidArgument("Incorrect buffer size $bufferSize"))
    }
    if (this@readSymlink.ReparseTag != IO_REPARSE_TAG_SYMLINK) {
        raise(BadFileDescriptor("Not a symlink"))
    }
    if (this@readSymlink.SymbolicLinkReparseBuffer.Flags.toInt() and SYMLINK_FLAG_RELATIVE != SYMLINK_FLAG_RELATIVE) {
        raise(BadFileDescriptor("Symlink should be relative"))
    }

    val offsetBytes = this@readSymlink.SymbolicLinkReparseBuffer.SubstituteNameOffset.toInt()
    val lengthBytes = this@readSymlink.SymbolicLinkReparseBuffer.SubstituteNameLength.toInt()

    val requiredSize = REPARSE_DATA_BUFFER_SYMLINK_PATH_BUFFER_OFFSET + offsetBytes + lengthBytes
    if (bufferSize < requiredSize) {
        raise(BadFileDescriptor("Symlink should be relative"))
    }

    val substituteName: String = this@readSymlink.SymbolicLinkReparseBuffer.PathBuffer.readChars(
        length = lengthBytes / 2,
        fromIndex = offsetBytes / 2,
    ).concatToString()

    return WindowsRealPath.create(substituteName).mapLeft { it.toCommonError() }
}

@Suppress("MagicNumber")
private fun maxSizes(): Sequence<Int> = sequence {
    var size: Int = MAX_PATH * sizeOf<WCHARVar>().toInt()
    while (size < MAXIMUM_REPARSE_DATA_BUFFER_SIZE) {
        yield(size)
        size = (size * 1.5).toInt().coerceAtMost(MAXIMUM_REPARSE_DATA_BUFFER_SIZE)
    }
    yield(size)
}

private fun Win32ErrorCode.toReadLinkError(): ReadLinkError = when (this.code.toInt()) {
    ERROR_ACCESS_DENIED -> AccessDenied("Can not read symlink target: access denied")
    ERROR_FILE_NOT_FOUND -> NoEntry("file not found")
    ERROR_INVALID_HANDLE -> InvalidArgument("Invalid handle")
    ERROR_INVALID_PARAMETER -> IoError("Invalid parameters")
    else -> IoError("Other error: $this")
}
