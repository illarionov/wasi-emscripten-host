/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.ext

import at.released.weh.common.ext.readU64Le
import at.released.weh.filesystem.windows.win32api.fileinfo.FileIdInfo
import kotlinx.io.bytestring.ByteString

internal fun FileIdInfo.get64bitInode(): Long = get64BitInodeFromFileId(fileId)

internal fun get64BitInodeFromFileId(fileId: ByteString): Long {
    val qLo = fileId.readU64Le(0)
    val qHi = fileId.readU64Le(8)
    return (qLo xor qHi).toLong()
}
