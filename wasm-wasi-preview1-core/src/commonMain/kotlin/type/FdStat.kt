/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.type

import at.released.weh.wasm.core.WasmValueType
import at.released.weh.wasm.core.WasmValueTypes.I32

/**
 * File descriptor attributes.
 *
 * @param fsFiletype File type.
 * @param fsFlags File descriptor flags.
 * @param fsRightsBase Rights that apply to this file descriptor.
 * @param fsRightsInheriting Maximum set of rights that may be installed on new file descriptors that are created
 * through this file descriptor, e.g., through `path_open`.
 */
@Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")
public data class FdStat(
    val fsFiletype: Filetype, // (field $fs_filetype $filetype)
    val fsFlags: FdFlags, // (field $fs_flags $fdflags)
    val fsRightsBase: Rights, // (field $fs_rights_base $rights)
    val fsRightsInheriting: Rights, // (field $fs_rights_inheriting $rights)
) {
    public companion object : WasiTypename {
        @WasmValueType
        override val wasmValueType: Int = I32
    }
}
