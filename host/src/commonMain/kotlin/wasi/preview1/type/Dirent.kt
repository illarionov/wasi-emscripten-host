/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.host.base.WasmValueType

/**
 * A directory entry.
 *
 * @param dNext The offset of the next directory entry stored in this directory.
 * @param dIno The serial number of the file referred to by this directory entry.
 * @param dNamlen The length of the name of the directory entry.
 * @param dType The type of the file referred to by this directory entry.
 */
@Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")
public data class Dirent(
    val dNext: Dircookie, // (field $d_next $dircookie)
    val dIno: Inode, // (field $d_ino $inode)
    val dNamlen: Dirnamlen, // (field $d_namlen $dirnamlen)
    val dType: Filetype, // (field $d_type $filetype)
) {
    public companion object : WasiTypename {
        override val wasmValueType: WasmValueType = WasiValueTypes.U32
    }
}
