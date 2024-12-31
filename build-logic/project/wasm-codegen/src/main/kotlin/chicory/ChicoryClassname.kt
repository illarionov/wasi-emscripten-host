/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chicory

import at.released.weh.gradle.wasm.codegen.util.classname.WehWasiPreview1ClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

internal object ChicoryClassname {
    const val PACKAGE = "com.dylibso.chicory"
    const val RUNTIME_PACKAGE = "$PACKAGE.runtime"
    const val WASM_TYPES_PACKAGE = "$PACKAGE.wasm.types"
    val HOST_FUNCTION = ClassName(RUNTIME_PACKAGE, "HostFunction")
    val INSTANCE = ClassName(RUNTIME_PACKAGE, "Instance")
    val VALUE_TYPE = ClassName(WASM_TYPES_PACKAGE, "ValueType")
    val VALUE_TYPE_I32 = VALUE_TYPE.member("I32")
    val VALUE_TYPE_I64 = VALUE_TYPE.member("I64")

    public object Bindings {
        const val CHICORY_BINDINGS_PACKAGE = "at.released.weh.bindings.chicory"
        const val ADAPTER_PACKAGE = "$CHICORY_BINDINGS_PACKAGE.host.module.wasi"
        val CHICORY_FUNCTIONS_CLASS_NAME = ClassName(ADAPTER_PACKAGE, "ChicoryWasiPreview1Functions")
        val CHICORY_MEMORY_PROVIDER_CLASS_NAME = ClassName("$CHICORY_BINDINGS_PACKAGE.memory", "ChicoryMemoryProvider")
        val VALUE_AS_WASM_ADDR = MemberName("at.released.weh.bindings.chicory.ext", "asWasmAddr")
        val WASI_MEMORY_READER_PROVIDER = Function1::class.asClassName().parameterizedBy(
            INSTANCE,
            WehWasiPreview1ClassName.WASI_MEMORY_READER,
        )
        val WASI_MEMORY_WRITER_PROVIDER = Function1::class.asClassName().parameterizedBy(
            INSTANCE,
            WehWasiPreview1ClassName.WASI_MEMORY_WRITER,
        )
    }
}
