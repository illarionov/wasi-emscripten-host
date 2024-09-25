/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.generator.ext

import com.squareup.kotlinpoet.ClassName

object WasmCoreClassName {
    const val PACKAGE = "at.released.weh.wasm.core"
    val HOST_FUNCTION = ClassName(PACKAGE, "HostFunction")
    val HOST_FUNCTION_TYPE = HOST_FUNCTION.nestedClass("HostFunctionType")
}
