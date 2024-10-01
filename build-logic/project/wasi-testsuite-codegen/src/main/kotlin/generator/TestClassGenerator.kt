/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen.generator

import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.KotlinTest.KOTLIN_IGNORE
import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.KotlinTest.KOTLIN_TEST
import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.KotlinxIo
import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.ROOT_PACKAGE
import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.WASI_TESTS_TESTSUITE_ROOT
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.util.Locale

internal class TestClassGenerator(
    private val runtimeBindings: WasmRuntimeBindings,
    private val subtrestType: SubtestType,
    private val testNames: List<String>,
    private val ignoredTestNames: Set<String>,
    private val generateJvmCompanionObjects: Boolean = false,
) {
    private val testClassName = ClassName(
        "$ROOT_PACKAGE.${runtimeBindings.name.lowercase().replace("_", "")}",
        formatClassName(runtimeBindings, subtrestType),
    )
    private val lazyFactoryClassName: ClassName = testClassName
        .nestedClass("LazyFactory")

    fun generate(): FileSpec = FileSpec.builder(testClassName)
        .addType(generateClass())
        .build()

    private fun generateClass(): TypeSpec {
        val functions = testNames.map {
            generateTestFunction(it)
        }

        val testExecutorFactoryClass = if (!generateJvmCompanionObjects) {
            runtimeBindings.testExecutorfactoryClassName
        } else {
            lazyFactoryClassName
        }

        return TypeSpec.classBuilder(testClassName).apply {
            superclass(ClassNames.WASI_TEST_SUITE_BASE_TEST)

            addSuperclassConstructorParameter(
                "⇥\nwasiTestsRoot = %T(%M,%S)",
                KotlinxIo.KOTLINX_PATH,
                WASI_TESTS_TESTSUITE_ROOT,
                subtrestType.testsuiteSubdir,
            )
            addSuperclassConstructorParameter(
                "\nwasmRuntimeExecutorFactory = %T()\n⇤",
                testExecutorFactoryClass,
            )
            addFunctions(functions)

            if (generateJvmCompanionObjects) {
                addType(generateLazyFactory())
                addType(generateJvmCompanion())
            }
        }.build()
    }

    private fun generateTestFunction(functionName: String): FunSpec = FunSpec.builder(
        formatTestFunctionName(functionName),
    ).apply {
        addAnnotation(KOTLIN_TEST)
        if (functionName in ignoredTestNames) {
            addAnnotation(KOTLIN_IGNORE)
        }
        addCode("return runTest(%S)", functionName)
    }.build()

    private fun generateLazyFactory(): TypeSpec = TypeSpec.classBuilder(lazyFactoryClassName).apply {
        addSuperinterface(ClassNames.RUNTIME_TEST_EXECUTOR_FACTORY)
        addFunction(
            FunSpec.builder("invoke")
                .addModifiers(OVERRIDE)
                .returns(ClassNames.RUNTIME_TEST_EXECUTOR)
                .addCode("""return checkNotNull(%N).invoke()""", "factoryInstance")
                .build(),
        )
        addFunction(
            FunSpec.builder("close")
                .addModifiers(OVERRIDE)
                .addCode("return Unit")
                .build(),
        )
    }.build()

    private fun generateJvmCompanion(): TypeSpec = TypeSpec.companionObjectBuilder().apply {
        val factoryinstanceProperty = PropertySpec.builder(
            "factoryInstance",
            ClassNames.RUNTIME_TEST_EXECUTOR_FACTORY.copy(nullable = true),
        )
            .mutable(true)
            .initializer("""null""")
            .build()

        addProperty(factoryinstanceProperty)
        addFunction(
            FunSpec.builder("setupFactory")
                .addAnnotation(ClassNames.KotlinJvm.JVM_STATIC)
                .addAnnotation(ClassNames.JunitTest.BEFORE_CLASS)
                .addCode("""%N = %T()""", factoryinstanceProperty.name, runtimeBindings.testExecutorfactoryClassName)
                .build(),
        )
        addFunction(
            FunSpec.builder("destroyFactory")
                .addAnnotation(ClassNames.KotlinJvm.JVM_STATIC)
                .addAnnotation(ClassNames.JunitTest.AFTER_CLASS)
                .addCode("%N?.close()\n%N = null", factoryinstanceProperty.name, factoryinstanceProperty.name)
                .build(),
        )
    }.build()

    companion object {
        fun formatClassName(
            runtimeBindings: WasmRuntimeBindings,
            subtrestType: SubtestType,
        ) = listOf(
            runtimeBindings.name.replace("_", ""),
            subtrestType.name,
            "TestSuite",
        ).joinToString("", transform = ::capitalizeAscii)

        fun formatTestFunctionName(
            functionName: String,
        ): String = functionName
            .split('_', '-')
            .joinToString("", prefix = "test", transform = ::capitalizeAscii)

        private fun capitalizeAscii(str: String): String = str
            .lowercase()
            .replaceFirstChar { it.titlecase(Locale.ROOT) }
    }
}
