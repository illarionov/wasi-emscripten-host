/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm

import at.released.weh.gradle.wasm.codegen.chasm.ChasmArgsFunctionHandles.WasiFunctionHandle
import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmBindingsClassname.CHASM_FUNCTIONS_CLASS_NAME
import at.released.weh.gradle.wasm.codegen.chasm.classname.ChasmShapesClassname
import at.released.weh.gradle.wasm.codegen.util.classname.SUPPRESS_CLASS_NAME
import at.released.weh.gradle.wasm.codegen.util.classname.WehHostClassname
import at.released.weh.gradle.wasm.codegen.util.classname.WehWasiPreview1ClassName
import at.released.weh.gradle.wasm.codegen.util.classname.WehWasmCoreClassName
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

internal class ChasmHostFunctionsAdapterGenerator(
    private val wasiTypenames: Map<String, WasiType>,
    private val wasiFunctions: List<WasiFunc>,
    private val outputDirectory: File,
    private val factoryFunctionName: String = "createWasiPreview1HostFunctions",
) {
    fun generate() {
        val spec = FileSpec.builder(CHASM_FUNCTIONS_CLASS_NAME)
            .addFunction(
                ChasmFactoryFunctionGenerator(
                    wasiTypenames = wasiTypenames,
                    wasiFunctions = wasiFunctions,
                    functionsClassName = CHASM_FUNCTIONS_CLASS_NAME,
                    factoryFunctionName = factoryFunctionName,
                ).generate(),
            )
            .addType(generateFunctionsClass())
            .build()
        spec.writeTo(outputDirectory)
    }

    private fun generateFunctionsClass(): TypeSpec = TypeSpec.classBuilder(CHASM_FUNCTIONS_CLASS_NAME).apply {
        addModifiers(PRIVATE)
        addAnnotation(AnnotationSpec.builder(SUPPRESS_CLASS_NAME).addMember("%S", "UNUSED_PARAMETER").build())
        primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter("host", WehHostClassname.EMBEDDER_HOST)
                .addParameter("memory", WehWasmCoreClassName.Memory.MEMORY_CLASS_NAME)
                .addParameter("wasiMemoryReader", WehWasiPreview1ClassName.WASI_MEMORY_READER)
                .addParameter("wasiMemoryWriter", WehWasiPreview1ClassName.WASI_MEMORY_WRITER)
                .build(),
        )
        addProperty(
            PropertySpec.builder("memory", WehWasmCoreClassName.Memory.MEMORY_CLASS_NAME, PRIVATE)
                .initializer("memory")
                .build(),
        )
        addProperty(
            PropertySpec
                .builder("wasiMemoryReader", WehWasiPreview1ClassName.WASI_MEMORY_READER, PRIVATE)
                .initializer("wasiMemoryReader")
                .build(),
        )
        addProperty(
            PropertySpec
                .builder("wasiMemoryWriter", WehWasiPreview1ClassName.WASI_MEMORY_WRITER, PRIVATE)
                .initializer("wasiMemoryWriter")
                .build(),
        )

        val functionHandles = ChasmArgsFunctionHandles(wasiTypenames, wasiFunctions).getFunctionHandles()

        functionHandles.forEach { funcHandleSpec ->
            addProperty(funcHandleSpec.handleProperty.asPropertySpec())
        }
        functionHandles.forEach { funcHandleSpec: WasiFunctionHandle ->
            addFunction(funcHandleSpec.chasmHostFunctionDeclaration())
        }

        addFunction(
            FunSpec.builder("toListOfReturnValues")
                .addModifiers(PRIVATE)
                .receiver(WehWasiPreview1ClassName.ERRNO)
                .returns(LIST.parameterizedBy(ChasmShapesClassname.EXECUTION_VALUE))
                .addCode("""return listOf(%T(this.code))""", ChasmShapesClassname.RUNTIME_NUMBER_VALUE_I32)
                .build(),
        )
    }.build()
}
