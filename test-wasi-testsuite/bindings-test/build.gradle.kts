/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import at.released.weh.gradle.multiplatform.test.setupCopyDirectoryToIosTestResources
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.JVM_ON_WINDOWS
import at.released.weh.gradle.wasi.testsuite.codegen.generator.WasmRuntimeBindings
import org.jetbrains.kotlin.gradle.plugin.ExecutionTaskHolder
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithHostTests
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest

plugins {
    id("at.released.weh.gradle.lint.android-lint-noagp")
    id("at.released.weh.gradle.multiplatform.kotlin")
    id("at.released.weh.gradle.multiplatform.test.jvm")
    id("at.released.weh.gradle.wasi.testsuite.codegen.test-generator")
    kotlin("plugin.serialization")
}

group = "at.released.weh"

private val wasiTestSuiteRoot = layout.projectDirectory.dir("../wasi-testsuite/tests")

setupCopyDirectoryToIosTestResources(provider { wasiTestSuiteRoot })

wasiTestsuiteTestGen {
    wasiTestsuiteTestsRoot = wasiTestSuiteRoot
    runtimes = setOf(
        WasmRuntimeBindings.CHASM,
        WasmRuntimeBindings.CHICORY,
        WasmRuntimeBindings.CHICORY_BYTEARRAY_MEMORY,
        WasmRuntimeBindings.GRAALVM,
    )
    cIgnores = listOf(
        TestIgnore("sock_shutdown-invalid_fd"),
        TestIgnore("sock_shutdown-not_sock"),
    )
    rustIgnores = listOf(
        // legacy, not used anywhere
        TestIgnore("fd_fdstat_set_rights"),

        // Fails on JVM for Windows because hardlinks to file must have the same inode,
        TestIgnore("path_link", setOf(JVM_ON_WINDOWS)),
    )
}

kotlin {
    jvm()
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    macosArm64()
    macosX64()
    linuxArm64()
    linuxX64()

    testableTargets.withType<KotlinNativeTargetWithHostTests> {
        testRuns.all {
            @Suppress("UNCHECKED_CAST")
            (this as ExecutionTaskHolder<KotlinNativeTest>).executionTask.configure {
                environment("WASI_TESTSUITE_ROOT", wasiTestSuiteRoot.asFile.absolutePath)
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.tempfolder)
            implementation(projects.host)
            implementation(projects.hostTestFixtures)
            implementation(projects.testIoBootstrap)
            implementation(projects.testLogger)
            implementation(projects.testFilesystemAssertions)
            implementation(projects.testIgnoreAnnotations)
        }
        jvmMain.dependencies {
            implementation(kotlin("test-junit"))
            implementation(libs.graalvm241.polyglot.wasm)
        }
        commonTest.dependencies {
            implementation(projects.bindingsChasmWasip1)
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(libs.chicory.wasi)
            implementation(projects.bindingsChicoryWasip1)
            implementation(projects.bindingsGraalvm241Wasip1)
        }
    }
}
