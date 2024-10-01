/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen.generator

import com.squareup.kotlinpoet.ClassName

public enum class WasmRuntimeBindings(
    val testExecutorfactoryClassName: ClassName,
) {
    CHASM(
        testExecutorfactoryClassName = ClassName(
            "at.released.weh.wasi.bindings.test.chasm.base",
            "ChasmRuntimeTestExecutor",
        ).nestedClass("Factory"),
    ),
    CHICORY(
        testExecutorfactoryClassName = ClassName(
            "at.released.weh.wasi.bindings.test.chicory.base",
            "ChicoryRuntimeTestExecutor",
        ).nestedClass("Factory"),
    ),
    CHICORY_NATIVE(
        testExecutorfactoryClassName = ClassName(
            "at.released.weh.wasi.bindings.test.chicory.base",
            "ChicoryNativeRuntimeTestExecutor",
        ).nestedClass("Factory"),
    ),
    GRAALVM(
        testExecutorfactoryClassName = ClassName(
            "at.released.weh.wasi.bindings.test.graalvm.base",
            "GraalvmRuntimeTestExecutor",
        ).nestedClass("Factory"),
    ),
    GRAALVM_NATIVE(
        testExecutorfactoryClassName = ClassName(
            "at.released.weh.wasi.bindings.test.graalvm.base",
            "GraalvmNativeRuntimeTestExecutor",
        ).nestedClass("Factory"),
    ),
}
