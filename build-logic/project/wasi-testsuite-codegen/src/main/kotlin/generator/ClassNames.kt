/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen.generator

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MemberName

internal object ClassNames {
    const val ROOT_PACKAGE: String = "at.released.weh.wasi.bindings.test"
    val WASI_TEST_SUITE_BASE_TEST = ClassName(ROOT_PACKAGE, "WasiTestSuiteBaseTest")
    val WASI_TESTS_TESTSUITE_ROOT = MemberName(
        "at.released.weh.wasi.bindings.test.ext",
        "wasiTestsuiteRoot",
    )

    object KotlinxIo {
        val KOTLINX_PATH = ClassName("kotlinx.io.files", "Path")
    }

    object KotlinTest {
        val KOTLIN_TEST = ClassName("kotlin.test", "Test")
        val KOTLIN_IGNORE = ClassName("kotlin.test", "Ignore")
    }
}
