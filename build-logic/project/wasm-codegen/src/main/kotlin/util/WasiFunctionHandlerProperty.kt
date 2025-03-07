/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.util

import at.released.weh.gradle.wasm.codegen.util.classname.WehWasiPreview1ClassName
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.PropertySpec

internal class WasiFunctionHandlerProperty private constructor(
    val func: WasiFunc,
    val propertyName: String,
    val className: ClassName,
    val initializer: CodeBlock = CodeBlock.of("""%T(%N)""", className, "host"),
) {
    fun asPropertySpec(): PropertySpec = PropertySpec.builder(propertyName, className, PRIVATE)
        .initializer(initializer)
        .build()

    companion object {
        operator fun invoke(wasiFunc: WasiFunc): WasiFunctionHandlerProperty {
            val wasiNameCamelCase = wasiFunc.export.toCamelCasePropertyName()
            val handleClassname = wasiFunc.export.toUpperCamelCaseClassName() + "FunctionHandle"
            return WasiFunctionHandlerProperty(
                func = wasiFunc,
                propertyName = "${wasiNameCamelCase}Handle",
                className = ClassName(WehWasiPreview1ClassName.FUNCTION_PACKAGE, handleClassname),
            )
        }
    }
}
