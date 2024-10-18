/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm.classname

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member

internal object ChasmShapesClassname {
    const val PACKAGE = "io.github.charlietap.chasm.embedding.shapes"
    val FUNCTION_TYPE = ClassName(PACKAGE, "FunctionType")
    val HOST_FUNCTION_CONTEXT = ClassName(PACKAGE, "HostFunctionContext")
    val IMPORT = ClassName(PACKAGE, "Import")
    val STORE = ClassName(PACKAGE, "Store")
    val VALUE = ClassName(PACKAGE, "Value")
    val VALUE_TYPE = ClassName(PACKAGE, "ValueType")
    val VALUE_TYPE_I32 = VALUE_TYPE.nestedClass("Number").member("I32")
    val VALUE_TYPE_I64 = VALUE_TYPE.nestedClass("Number").member("I64")
    val CHASM_EMBEDDING_FUNCTION = MemberName("io.github.charlietap.chasm.embedding", "function")
}
