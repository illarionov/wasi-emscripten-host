/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.fileinfo

import at.released.weh.filesystem.platform.windows.FILE_ID_INFO
import at.released.weh.filesystem.windows.win32api.ext.asByteString
import kotlinx.io.bytestring.ByteString

internal data class FileIdInfo(
    val volumeSerialNumber: ULong,
    val fileId: ByteString,
) {
    init {
        check(fileId.size == 16)
    }

    internal companion object {
        fun create(info: FILE_ID_INFO) = FileIdInfo(
            volumeSerialNumber = info.VolumeSerialNumber,
            fileId = info.FileId.asByteString(),
        )
    }
}
