/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import platform.windows.CreateSymbolicLinkW
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_ALREADY_EXISTS
import platform.windows.ERROR_DIRECTORY
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.ERROR_PRIVILEGE_NOT_HELD
import platform.windows.PathFileExistsW
import platform.windows.SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE

internal fun windowsCreateSymbolicLink(
    oldPath: WindowsRealPath,
    newPath: WindowsRealPath,
    type: SymlinkType,
): Either<SymlinkError, Unit> {
    val flags = type.mask
    if (CreateSymbolicLinkW(newPath.kString, oldPath.kString, flags).toInt() == 0) {
        val lastError = Win32ErrorCode.getLast()
        if (lastError.code == ERROR_PRIVILEGE_NOT_HELD.toUInt()) {
            val newFlags = flags or SYMBOLIC_LINK_FLAG_ALLOW_UNPRIVILEGED_CREATE.toUInt()
            if (CreateSymbolicLinkW(newPath.kString, oldPath.kString, newFlags).toInt() == 0) {
                return getSymlinkError(Win32ErrorCode.getLast(), newPath).left()
            }
        } else {
            return getSymlinkError(lastError, newPath).left()
        }
    }
    return Unit.right()
}

private fun getSymlinkError(
    code: Win32ErrorCode,
    newPath: WindowsRealPath,
): SymlinkError = if (code.code.toInt() == ERROR_ACCESS_DENIED && PathFileExistsW(newPath.kString) != 0) {
    Exists("Path already exists")
} else {
    code.toSymlinkError()
}

private fun Win32ErrorCode.toSymlinkError(): SymlinkError = when (this.code.toInt()) {
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    ERROR_ACCESS_DENIED -> AccessDenied("Cannot Create symlink, access denied")
    ERROR_ALREADY_EXISTS -> Exists("Path already exists")
    ERROR_FILE_NOT_FOUND -> NoEntry("source path not found")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect path")
    ERROR_DIRECTORY -> NotDirectory("Invalid directory name")
    ERROR_PRIVILEGE_NOT_HELD -> AccessDenied("Cannot Create symlink, a required privilege is not held")
    else -> IoError("Other error: $this")
}

internal enum class SymlinkType {
    SYMLINK_TO_FILE,
    SYMLINK_TO_DIRECTORY,
}

private val SymlinkType.mask: UInt
    get() = when (this) {
        SymlinkType.SYMLINK_TO_FILE -> 0
        SymlinkType.SYMLINK_TO_DIRECTORY -> platform.windows.SYMBOLIC_LINK_FLAG_DIRECTORY
    }.toUInt()
