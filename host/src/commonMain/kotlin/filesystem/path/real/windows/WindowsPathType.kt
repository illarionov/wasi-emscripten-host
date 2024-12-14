/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import at.released.weh.filesystem.path.real.windows.WindowsPathType.CURRENT_DRIVE_RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.DRIVE_ABSOLUTE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.DRIVE_CURRENT_DIRECTORY_RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.LOCAL_DEVICE_LITERAL
import at.released.weh.filesystem.path.real.windows.WindowsPathType.LOCAL_DEVICE_NORMALIZED
import at.released.weh.filesystem.path.real.windows.WindowsPathType.RELATIVE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.ROOT_LOCAL_DEVICE
import at.released.weh.filesystem.path.real.windows.WindowsPathType.UNC

internal enum class WindowsPathType {
    /**
     * Drive absolute path: `C:\Program Files (x86)`, `D:/$RECYCLE.BIN`, `E:\Documents And Settings\desktop.ini`, etc
     */
    DRIVE_ABSOLUTE,

    /**
     * Relative to the current directory path: `.\Public`, `../Users`, 'Windows\System32/LogFiles', etc
     */
    RELATIVE,

    /**
     * Path relative to the current directory of the drive: `C:Public`, `D:Users/`, etc
     */
    DRIVE_CURRENT_DIRECTORY_RELATIVE,

    /**
     * Path that is resolved relative to the drive of the current directory: `\Windows\System32`,
     * `\Documents And Settings`, ...
     */
    CURRENT_DRIVE_RELATIVE,

    /**
     * UNC absolute path: `\\server\share\ABC\DEF`, etc
     */
    UNC,

    /**
     * Local-device path with normalization: `\\.\C:\Windows`
     */
    LOCAL_DEVICE_NORMALIZED,

    /**
     * Local-device path without normalization: `\\?\C:\Windows`
     */
    LOCAL_DEVICE_LITERAL,

    /**
     * Paths started with "\\?" or "\\." which are not other types
     */
    ROOT_LOCAL_DEVICE,
}

internal val WindowsPathType.prefixLength: Int
    get() = when (this) {
        DRIVE_ABSOLUTE -> 3
        RELATIVE -> 0
        DRIVE_CURRENT_DIRECTORY_RELATIVE -> 2
        CURRENT_DRIVE_RELATIVE -> 1
        UNC -> 2
        LOCAL_DEVICE_NORMALIZED -> 4
        LOCAL_DEVICE_LITERAL -> 4
        ROOT_LOCAL_DEVICE -> 3
    }

// Common implementation of RtlDetermineDosPathNameType_U
@Suppress("CyclomaticComplexMethod")
internal fun getWindowsPathType(path: String): WindowsPathType {
    when (path.length) {
        0 -> return RELATIVE
        1 -> return if (path[0].isSlash()) {
            CURRENT_DRIVE_RELATIVE
        } else {
            RELATIVE
        }
    }
    return when {
        path.length > 3 && path[0].isSlash() && path[1].isSlash() && path[2] == '?' && path[3].isSlash() ->
            LOCAL_DEVICE_LITERAL

        path.length > 3 && path[0].isSlash() && path[1].isSlash() && path[2] == '.' && path[3].isSlash() ->
            LOCAL_DEVICE_NORMALIZED

        path.length == 3 && path[0].isSlash() && path[1].isSlash() && (path[2] == '.' || path[2] == '?') ->
            ROOT_LOCAL_DEVICE

        path.length > 3 && path[0].isSlash() && path[1].isSlash() && (path[2] == '.' || path[2] == '?') &&
                path[3] == 0.toChar() -> ROOT_LOCAL_DEVICE

        path[0].isSlash() && path[1].isSlash() -> UNC
        path[0].isSlash() -> CURRENT_DRIVE_RELATIVE
        path[1] == ':' -> when {
            path[0].isValidDriveLetter() && path.length > 2 && path[2].isSlash() -> DRIVE_ABSOLUTE
            path[0].isValidDriveLetter() -> DRIVE_CURRENT_DIRECTORY_RELATIVE
            else -> RELATIVE
        }

        else -> RELATIVE
    }
}

private fun Char.isSlash() = this == '\\' || this == '/'

private fun Char.isValidDriveLetter() = this != 0.toChar()
