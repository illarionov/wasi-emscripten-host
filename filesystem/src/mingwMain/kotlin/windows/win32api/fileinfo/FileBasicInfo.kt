/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.windows.win32api.ext.asStructTimespec
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import platform.windows.FILE_BASIC_INFO

internal data class FileBasicInfo(
    val creationTime: StructTimespec,
    val lastAccessTime: StructTimespec,
    val lastWriteTime: StructTimespec,
    val changeTime: StructTimespec,
    val fileAttributes: FileAttributes,
) {
    internal companion object {
        fun create(
            info: FILE_BASIC_INFO,
        ): FileBasicInfo = FileBasicInfo(
            creationTime = info.CreationTime.asStructTimespec,
            lastAccessTime = info.LastAccessTime.asStructTimespec,
            lastWriteTime = info.LastWriteTime.asStructTimespec,
            changeTime = info.ChangeTime.asStructTimespec,
            fileAttributes = FileAttributes(info.FileAttributes),
        )
    }
}
