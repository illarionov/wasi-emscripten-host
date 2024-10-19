/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.graalvm

import com.squareup.kotlinpoet.ClassName

internal object GraalvmClassname {
    object Truffle {
        const val PACKAGE = "com.oracle.truffle.api"
        val VIRTUAL_FRAME = ClassName("$PACKAGE.frame", "VirtualFrame")
        val TRUFFLE_BOUNDARY = ClassName("$PACKAGE.CompilerDirectives", "TruffleBoundary")
    }

    object Wasm {
        const val PACKAGE = "org.graalvm.wasm"
        val WASM_LANGUAGE = ClassName(PACKAGE, "WasmLanguage")
        val WASM_MODULE = ClassName(PACKAGE, "WasmModule")
        val WASM_CONTEXT = ClassName(PACKAGE, "WasmContext")
        val WASM_INSTANCE = ClassName(PACKAGE, "WasmInstance")
        val WASM_MEMORY = ClassName("$PACKAGE.memory", "WasmMemory")
    }
}
