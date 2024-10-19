/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.graalvm

import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlerProperty
import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlersExt.NO_MEMORY_FUNCTIONS
import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlersExt.WASI_MEMORY_READER_FUNCTIONS
import at.released.weh.gradle.wasm.codegen.util.WasiFunctionHandlersExt.WASI_MEMORY_WRITER_FUNCTIONS
import at.released.weh.gradle.wasm.codegen.util.classname.WehHostClassname
import at.released.weh.gradle.wasm.codegen.util.classname.WehWasmCoreClassName
import at.released.weh.gradle.wasm.codegen.util.toCamelCasePropertyName
import at.released.weh.gradle.wasm.codegen.util.toUpperCamelCaseClassName
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.HANDLE
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.POINTER
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S16
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S32
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S64
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.S8
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U16
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U32
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U64
import at.released.weh.gradle.wasm.codegen.witx.helper.WasiBaseTypeResolver.WasiBaseWasmType.U8
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiFunc
import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiType
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier.INTERNAL
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.NOTHING
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

internal class GraalvmHostFunctionsGenerator(
    private val wasiTypenames: Map<String, WasiType>,
    private val wasiFunctions: List<WasiFunc>,
    private val outputDirectory: File,
) {
    private val baseTypeResolver = WasiBaseTypeResolver(wasiTypenames)

    fun generate() {
        val specs = generateFunctions()
        specs.forEach {
            it.writeTo(outputDirectory)
        }
    }

    private fun generateFunctions(): Iterable<FileSpec> = wasiFunctions.map {
        generateFunctionFileSpec(it)
    }

    private fun generateFunctionFileSpec(func: WasiFunc): FileSpec =
        FileSpec.builder(func.graalFunctionClassName).apply {
            addType(generateWasiWasmNode(func))
        }.build()

    private fun generateWasiWasmNode(func: WasiFunc): TypeSpec =
        TypeSpec.classBuilder(func.graalFunctionClassName).apply {
            addModifiers(INTERNAL)
            primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("language", GraalvmClassname.Wasm.WASM_LANGUAGE)
                    .addParameter("module", GraalvmClassname.Wasm.WASM_MODULE)
                    .addParameter("host", WehHostClassname.EMBEDDER_HOST)
                    .build(),
            )

            val functionHandle = WasiFunctionHandlerProperty(func)
            superclass(BASE_WASM_NODE_CLASSNAME.parameterizedBy(functionHandle.className))
            addSuperclassConstructorParameter("%N", "language")
            addSuperclassConstructorParameter("%N", "module")
            addSuperclassConstructorParameter(functionHandle.initializer)
            addFunction(generateExecuteWithContextFunction(func))
            addFunction(generateProxyFunction(func))
        }.build()

    private fun generateExecuteWithContextFunction(func: WasiFunc): FunSpec {
        val handleArgs: List<Pair<String, MemberName>> =
            baseTypeResolver.getFuncInputArgs(func).mapIndexed { index, (baseType: WasiBaseWasmType, comment) ->
                val converterFunc = when (baseType) {
                    POINTER -> ARGS_AS_WASM_ADDR
                    S8, U8 -> ARGS_AS_BYTE
                    S16, U16 -> ARGS_AS_SHORT
                    S32, U32, HANDLE -> ARGS_AS_INT
                    S64, U64 -> ARGS_AS_LONG
                }
                "\nargs.%M($index), /* $comment */" to converterFunc
            }

        val allArgs: List<Pair<String, Any>> = buildList {
            if (func.export !in NO_MEMORY_FUNCTIONS) {
                add("\n%N(frame)," to "memory")
            }
            addAll(handleArgs)
        }

        return FunSpec.builder("executeWithContext").apply {
            addModifiers(OVERRIDE)
            addParameter("frame", GraalvmClassname.Truffle.VIRTUAL_FRAME)
            addParameter("context", GraalvmClassname.Wasm.WASM_CONTEXT)
            addParameter("instance", GraalvmClassname.Wasm.WASM_INSTANCE)
            returns(ANY)

            if (allArgs.isNotEmpty()) {
                addCode("val args = frame.arguments\n")
            }

            val returnOp = if (func.result != null) {
                "return "
            } else {
                ""
            }

            addCode(
                format = allArgs.joinToString(
                    separator = "",
                    prefix = "$returnOp%N(⇥⇥⇥",
                    postfix = "\n⇤)⇤⇤",
                    transform = Pair<String, *>::first,
                ),
                args = (listOf(func.proxyFunctionName) + allArgs.map(Pair<String, Any>::second)).toTypedArray(),
            )
        }.build()
    }

    private fun generateProxyFunction(func: WasiFunc): FunSpec = FunSpec.builder(func.proxyFunctionName).apply {
        returns(
            if (func.result != null) {
                INT
            } else {
                NOTHING
            },
        )
        addModifiers(PRIVATE)
        addAnnotation(GraalvmClassname.Truffle.TRUFFLE_BOUNDARY)

        if (func.export !in NO_MEMORY_FUNCTIONS) {
            addParameter("memory", GraalvmClassname.Wasm.WASM_MEMORY)
        }

        baseTypeResolver.getFuncInputArgs(func).forEach { (baseType: WasiBaseWasmType, identifier, _: String) ->
            addParameter(identifier, baseType.argumentTypeName)
        }

        if (func.export !in NO_MEMORY_FUNCTIONS) {
            addCode("val hostMemory = memory.toHostMemory()\n")
        }

        if (func.export in WASI_MEMORY_READER_FUNCTIONS) {
            addCode(
                "val wasiMemoryReader = %T(hostMemory, handle.host.fileSystem)\n",
                GRAAL_MEMORY_READER,
            )
        }

        if (func.export in WASI_MEMORY_WRITER_FUNCTIONS) {
            addCode(
                "val wasiMemoryWriter = %T(hostMemory, handle.host.fileSystem, handle.host.rootLogger)\n",
                GRAAL_MEMORY_WRITER,
            )
        }

        val allArgs: List<String> = buildList {
            if (func.export !in NO_MEMORY_FUNCTIONS) {
                add("hostMemory")
            }
            if (func.export in WASI_MEMORY_READER_FUNCTIONS) {
                add("wasiMemoryReader")
            }
            if (func.export in WASI_MEMORY_WRITER_FUNCTIONS) {
                add("wasiMemoryWriter")
            }
            baseTypeResolver.getFuncInputArgs(func).forEach { (_, identifier, _) -> add(identifier) }
        }

        val code = if (func.result != null) {
            ".code"
        } else {
            ""
        }

        addCode(
            format = allArgs.joinToString(
                separator = ",",
                prefix = "return handle.execute(",
                postfix = ")$code",
            ) { "%N" },
            args = allArgs.toTypedArray(),
        )
    }.build()

    companion object {
        private const val PACKAGE_NAME = "at.released.weh.bindings.graalvm241"
        private const val MEMORY_PACKAGE_NAME = "$PACKAGE_NAME.host.memory"
        private const val HOST_MODULE_PACKAGE_NAME = "$PACKAGE_NAME.host.module"
        private const val FUNCTIONS_PACKAGE_NAME = "$HOST_MODULE_PACKAGE_NAME.wasi.function"
        private const val EXT_PACKAGE_NAME = "$PACKAGE_NAME.ext"
        private val BASE_WASM_NODE_CLASSNAME = ClassName("$HOST_MODULE_PACKAGE_NAME.wasi", "BaseWasiWasmNode")
        private val ARGS_AS_BYTE = MemberName(EXT_PACKAGE_NAME, "getArgAsByte")
        private val ARGS_AS_SHORT = MemberName(EXT_PACKAGE_NAME, "getArgAsShort")
        private val ARGS_AS_INT = MemberName(EXT_PACKAGE_NAME, "getArgAsInt")
        private val ARGS_AS_LONG = MemberName(EXT_PACKAGE_NAME, "getArgAsLong")
        private val ARGS_AS_WASM_ADDR = MemberName(EXT_PACKAGE_NAME, "getArgAsWasmPtr")
        private val GRAAL_MEMORY_READER = ClassName(MEMORY_PACKAGE_NAME, "GraalInputStreamWasiMemoryReader")
        private val GRAAL_MEMORY_WRITER = ClassName(MEMORY_PACKAGE_NAME, "GraalOutputStreamWasiMemoryWriter")

        private val WasiFunc.graalFunctionClassName: ClassName
            get() = ClassName(
                FUNCTIONS_PACKAGE_NAME,
                this.export.toUpperCamelCaseClassName(),
            )

        private val WasiFunc.proxyFunctionName: String
            get() = this.export.toCamelCasePropertyName()

        private val WasiBaseWasmType.argumentTypeName: TypeName
            get() = when (this) {
                POINTER -> WehWasmCoreClassName.WASM_PTR
                S8, U8 -> BYTE
                S16, U16 -> SHORT
                S32, U32, HANDLE -> INT
                S64, U64 -> LONG
            }
    }
}
