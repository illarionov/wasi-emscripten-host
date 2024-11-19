/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import platform.windows.FILE_STANDARD_INFO

internal data class FileStandardInfo(
    val allocationSize: Long,
    val endOfFile: Long,
    val numberOfLinks: UInt,
    val isDeletePending: Boolean,
    val isDirectory: Boolean,
) {
    internal companion object {
        fun create(
            info: FILE_STANDARD_INFO
        ): FileStandardInfo = FileStandardInfo(
            allocationSize = info.AllocationSize.QuadPart,
            endOfFile = info.EndOfFile.QuadPart,
            numberOfLinks = info.NumberOfLinks,
            isDeletePending = info.DeletePending.toInt() != 0,
            isDirectory = info.Directory.toInt() != 0
        )
    }
}
