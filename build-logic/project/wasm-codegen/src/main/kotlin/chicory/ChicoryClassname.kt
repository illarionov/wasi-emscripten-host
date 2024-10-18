/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chicory

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member

internal object ChicoryClassname {
    const val PACKAGE = "com.dylibso.chicory"
    const val RUNTIME_PACKAGE = "$PACKAGE.runtime"
    const val WASM_TYPES_PACKAGE = "$PACKAGE.wasm.types"
    val HOST_FUNCTION = ClassName(RUNTIME_PACKAGE, "HostFunction")
    val INSTANCE = ClassName(RUNTIME_PACKAGE, "Instance")
    val VALUE = ClassName(WASM_TYPES_PACKAGE, "Value")
    val VALUE_AS_BYTE = VALUE.member("asByte")
    val VALUE_AS_INT = VALUE.member("asInt")
    val VALUE_AS_LONG = VALUE.member("asLong")
    val VALUE_AS_SHORT = VALUE.member("asShort")
    val VALUE_TYPE = ClassName(WASM_TYPES_PACKAGE, "ValueType")
    val VALUE_TYPE_I32 = VALUE_TYPE.member("I32")
    val VALUE_TYPE_I64 = VALUE_TYPE.member("I64")

    public object Bindings {
        const val ADAPTER_PACKAGE = "at.released.weh.bindings.chicory.host.module.wasi"
        val CHICORY_FUNCTIONS_CLASS_NAME = ClassName(ADAPTER_PACKAGE, "ChicoryWasiPreview1Functions")
        val VALUE_AS_WASM_ADDR = MemberName("at.released.weh.bindings.chicory.ext", "asWasmAddr")
    }
}
