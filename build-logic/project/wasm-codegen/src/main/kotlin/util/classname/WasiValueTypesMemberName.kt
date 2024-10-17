/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.util.classname

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

@Suppress("VARIABLE_NAME_INCORRECT")
object WasiValueTypesMemberName {
    const val WASI_PREVIEW1_PACKAGE = "at.released.weh.wasi.preview1"
    val WASI_VALUE_TYPES = ClassName(WASI_PREVIEW1_PACKAGE, "WasiValueTypes")
    val POINTER = MemberName(WehWasmCoreClassName.PACKAGE, "POINTER")
    val U8 = MemberName(WASI_VALUE_TYPES, "U8")
    val U16 = MemberName(WASI_VALUE_TYPES, "U16")
    val S32 = MemberName(WASI_VALUE_TYPES, "S32")
    val U32 = MemberName(WASI_VALUE_TYPES, "U32")
    val S64 = MemberName(WASI_VALUE_TYPES, "S64")
    val U64 = MemberName(WASI_VALUE_TYPES, "U64")
    val HANDLE = MemberName(WASI_VALUE_TYPES, "HANDLE")
}
