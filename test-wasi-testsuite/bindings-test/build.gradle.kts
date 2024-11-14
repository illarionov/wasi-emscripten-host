/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import at.released.weh.gradle.multiplatform.test.setupCopyDirectoryToIosTestResources
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.APPLE
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.JVM_ON_LINUX
import at.released.weh.gradle.wasi.testsuite.codegen.TestIgnore.IgnoreTarget.JVM_ON_MACOS
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
        WasmRuntimeBindings.GRAALVM,
    )
    cIgnores = listOf(
        TestIgnore("sock_shutdown-invalid_fd"),
        TestIgnore("sock_shutdown-not_sock"),
    )
    rustIgnores = listOf(
        // legacy, not used anywhere
        TestIgnore("fd_fdstat_set_rights"),

        // resolveBeneath is not yet implemented
        TestIgnore("interesting_paths", setOf(APPLE, JVM_ON_WINDOWS)),

        // Fails on JVM for Windows because hardlinks to file must have the same inodeTestIgnore("path_link"),
        TestIgnore("path_link", setOf(JVM_ON_WINDOWS)),

        // Not yes implemented
        TestIgnore("poll_oneoff_stdio"),

        // Fails on JVM for Linux because JVM rounds timestamps of symlinks to microseconds (JDK-8343417)
        TestIgnore("symlink_filestat", setOf(JVM_ON_LINUX, JVM_ON_MACOS)),
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
    // mingwX64()

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
            implementation(projects.filesystemTestFixtures)
            implementation(projects.host)
            implementation(projects.testTempfolder)
            implementation(projects.testIoBootstrap)
            implementation(projects.testLogger)
            implementation(projects.testFilesystemAssertions)
        }
        jvmMain.dependencies {
            implementation(kotlin("test-junit"))
        }
        commonTest.dependencies {
            implementation(projects.bindingsChasm)
            implementation(projects.testIgnoreAnnotations)
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(libs.chicory.wasi)
            implementation(projects.bindingsChicory)
            implementation(projects.bindingsGraalvm241)
        }
    }
}
