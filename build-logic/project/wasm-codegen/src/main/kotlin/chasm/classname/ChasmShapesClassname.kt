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
    val HOST_FUNCTION_CONTEXT = ClassName(PACKAGE, "HostFunctionContext")
    val IMPORT = ClassName(PACKAGE, "Import")
    val STORE = ClassName(PACKAGE, "Store")
    val CHASM_EMBEDDING_FUNCTION = MemberName("io.github.charlietap.chasm.embedding", "function")
    val EXECUTION_VALUE = ClassName("io.github.charlietap.chasm.executor.runtime.value", "ExecutionValue")
    val RUNTIME_NUMBER_VALUE = ClassName("io.github.charlietap.chasm.executor.runtime.value", "NumberValue")
    val RUNTIME_NUMBER_VALUE_I32 = RUNTIME_NUMBER_VALUE.nestedClass("I32")

    internal object AstType {
        const val PACKAGE = "io.github.charlietap.chasm.ast.type"
        val AST_FUNCTION_TYPE = ClassName(PACKAGE, "FunctionType")
        val AST_NUMBER_TYPE = ClassName(PACKAGE, "NumberType")
        val AST_NUMBER_TYPE_I32 = AST_NUMBER_TYPE.member("I32")
        val AST_NUMBER_TYPE_I64 = AST_NUMBER_TYPE.member("I64")
        val AST_RESULT_TYPE = ClassName(PACKAGE, "ResultType")
        val AST_VALUE_TYPE = ClassName(PACKAGE, "ValueType")
        val VALUE_TYPE_NUMBER = AST_VALUE_TYPE.nestedClass("Number")
    }
}
