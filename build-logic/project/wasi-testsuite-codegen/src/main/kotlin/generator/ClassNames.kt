/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.asClassName

internal object ClassNames {
    const val ROOT_PACKAGE: String = "at.released.weh.wasi.bindings.test"
    val WASI_TEST_SUITE_BASE_TEST = ClassName(ROOT_PACKAGE, "WasiTestSuiteBaseTest")
    val WASI_TESTS_TESTSUITE_ROOT = MemberName(
        "at.released.weh.wasi.bindings.test.ext",
        "wasiTestsuiteRoot",
    )
    val RUNTIME_TEST_EXECUTOR = ClassName("$ROOT_PACKAGE.runner", "RuntimeTestExecutor")
    val RUNTIME_TEST_EXECUTOR_FACTORY = RUNTIME_TEST_EXECUTOR.nestedClass("Factory")

    object KotlinJvm {
        val JVM_STATIC = JvmStatic::class.asClassName()
    }

    object KotlinxIo {
        val KOTLINX_PATH = ClassName("kotlinx.io.files", "Path")
    }

    object KotlinTest {
        val KOTLIN_TEST = ClassName("kotlin.test", "Test")
        val KOTLIN_IGNORE = ClassName("kotlin.test", "Ignore")
    }

    object JunitTest {
        val BEFORE_CLASS = ClassName("org.junit", "BeforeClass")
        val AFTER_CLASS = ClassName("org.junit", "AfterClass")
    }
}
