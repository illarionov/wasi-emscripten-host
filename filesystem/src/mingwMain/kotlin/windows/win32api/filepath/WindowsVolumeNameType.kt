/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.filepath

import at.released.weh.filesystem.windows.win32api.filepath.WindowsVolumeNameType.DOS
import at.released.weh.filesystem.windows.win32api.filepath.WindowsVolumeNameType.GUID
import at.released.weh.filesystem.windows.win32api.filepath.WindowsVolumeNameType.NONE
import at.released.weh.filesystem.windows.win32api.filepath.WindowsVolumeNameType.NT
import platform.windows.VOLUME_NAME_DOS
import platform.windows.VOLUME_NAME_GUID
import platform.windows.VOLUME_NAME_NONE
import platform.windows.VOLUME_NAME_NT

internal enum class WindowsVolumeNameType {
    /**
     * Path prefixed by "\\?\" with drive letter
     */
    DOS,

    /**
     * GUID path prefixed by "\\?\Volume{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}\"
     */
    GUID,

    /**
     * Path without drive information
     */
    NONE,

    /**
     * NT device object path prefixed by device name: "\Device\HarddiskVolume1\"
     */
    NT,
}

internal val WindowsVolumeNameType.win32Mask: Int
    get() = when (this) {
        DOS -> VOLUME_NAME_DOS
        GUID -> VOLUME_NAME_GUID
        NONE -> VOLUME_NAME_NONE
        NT -> VOLUME_NAME_NT
    }
