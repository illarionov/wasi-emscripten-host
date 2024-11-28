/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.volume

import platform.windows.FILE_READ_ONLY_VOLUME

/**
 * Flags associated with the specified file system as returned by the GetVolumeInformationByHandleW
 * or GetVolumeInformationW functions
 */
internal value class FileSystemFlags(
    val mask: UInt,
) {
    val isReadOnly: Boolean
        get() = mask and FILE_READ_ONLY_VOLUME.toUInt() == FILE_READ_ONLY_VOLUME.toUInt()

    override fun toString(): String {
        return "FileSystemFlags(mask=0x${mask.toString(16)})"
    }
}
