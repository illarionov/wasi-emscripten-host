/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm.generator.classname

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

object ChasmBindingsClassname {
    const val PACKAGE = "at.released.weh.bindings.chasm.module.wasi"
    const val CHASM_WASI_BUILDER_FILE_NAME = "WasiSnapshotPreview1ModuleBuilder"
    val CHASM_MEMORY_ADAPTER = ClassName("at.released.weh.bindings.chasm.memory", "ChasmMemoryAdapter")

    object ChasmExt {
        const val PACKAGE = "at.released.weh.bindings.chasm.ext"
        val VALUE_AS_BYTE = MemberName(PACKAGE, "asByte")
        val VALUE_AS_SHORT = MemberName(PACKAGE, "asShort")
        val VALUE_AS_INT = MemberName(PACKAGE, "asInt")
        val VALUE_AS_LONG = MemberName(PACKAGE, "asLong")
        val VALUE_AS_WASM_ADDR = MemberName(PACKAGE, "asWasmAddr")
    }
}
