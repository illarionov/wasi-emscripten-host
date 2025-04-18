/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.util.classname

import at.released.weh.gradle.wasm.codegen.util.classname.TypedefAnnotationExt.TypedefAnnotationType.INT_DEF
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName

internal object TypedefAnnotationExt {
    val defaultTarget: List<AnnotationTarget> = listOf(
        AnnotationTarget.FIELD,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.LOCAL_VARIABLE,
        AnnotationTarget.PROPERTY,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER,
        AnnotationTarget.VALUE_PARAMETER,
    )

    fun createTypedefAnnotation(
        type: TypedefAnnotationType = INT_DEF,
        isFlag: Boolean = true,
        values: List<MemberName> = emptyList(),
    ): AnnotationSpec = AnnotationSpec.builder(type.className).apply {
        if (isFlag) {
            addMember("flag = true")
        }
        if (values.isNotEmpty()) {
            val valueBlock = CodeBlock.builder()
                .add("value = [⇥\n")
                .apply {
                    values.forEach { value ->
                        add("%M,\n", value)
                    }
                }
                .add("⇤]")
                .build()

            addMember(valueBlock)
        }
    }.build()

    enum class TypedefAnnotationType(val className: ClassName) {
        INT_DEF(ClassName("at.released.weh.common.api.typedef", "WasiEmscriptenHostIntDef")),
        LONG_DEF(ClassName("at.released.weh.common.api.typedef", "WasiEmscriptenHostLongDef")),
    }
}
