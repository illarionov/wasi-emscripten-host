/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen.generator

import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.APPLE
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.IOS
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.JVM
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.JVM_ON_LINUX
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.JVM_ON_MACOS
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.JVM_ON_WINDOWS
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.LINUX
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.MACOS
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.MINGW
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.NATIVE
import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.KotlinTest.KOTLIN_IGNORE
import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.WehDynamicIgnoreTestMember
import at.released.weh.gradle.wasi.testsuite.codegen.generator.ClassNames.WehTestIgnoreAnnotation
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName

internal class IgnoredTests(
    ignoredTests: Set<TestIgnore>,
) {
    private val ignores: Map<String, TestIgnore> = ignoredTests.associateBy(TestIgnore::name)

    fun addAnnotationsForStaticIgnores(
        funcSpecBuilder: FunSpec.Builder,
        functionName: String,
    ) {
        val ignoreTargets: Set<IgnoreTarget> = ignores[functionName]?.targets ?: return
        if (ignoreTargets.isEmpty()) {
            // Ignore all tests
            funcSpecBuilder.addAnnotation(KOTLIN_IGNORE)
            return
        }
        ignoreTargets.filter { it.isStatic }.forEach { ignoreTarget ->
            val ignoreAnnotation = when (ignoreTarget) {
                APPLE -> WehTestIgnoreAnnotation.IGNORE_APPLE
                IOS -> WehTestIgnoreAnnotation.IGNORE_IOS
                JVM -> WehTestIgnoreAnnotation.IGNORE_JVM
                LINUX -> WehTestIgnoreAnnotation.IGNORE_LINUX
                MACOS -> WehTestIgnoreAnnotation.IGNORE_MACOS
                MINGW -> WehTestIgnoreAnnotation.IGNORE_MINGW
                NATIVE -> WehTestIgnoreAnnotation.IGNORE_NATIVE
                else -> error("Should not be called")
            }
            funcSpecBuilder.addAnnotation(ignoreAnnotation)
        }
    }

    fun getDynamicIgnoresMemberNames(
        functionName: String,
    ): Set<MemberName> {
        val ignoreTargets: Set<IgnoreTarget> = ignores[functionName]?.targets ?: return emptySet()
        return ignoreTargets.mapNotNullTo(mutableSetOf()) { target: IgnoreTarget ->
            when (target) {
                JVM_ON_LINUX -> WehDynamicIgnoreTestMember.JVM_ON_LINUX
                JVM_ON_MACOS -> WehDynamicIgnoreTestMember.JVM_ON_MACOS
                JVM_ON_WINDOWS -> WehDynamicIgnoreTestMember.JVM_ON_WINDOWS
                else -> null
            }
        }
    }
}
