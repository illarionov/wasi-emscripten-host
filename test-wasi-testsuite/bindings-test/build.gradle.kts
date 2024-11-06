/*
 * Copyright 2024, the wasm-sqlite-open-helper project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

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

wasiTestsuiteTestGen {
    wasiTestsuiteTestsRoot = wasiTestSuiteRoot
    runtimes = setOf(
        WasmRuntimeBindings.CHASM,
        WasmRuntimeBindings.CHICORY,
//        WasmRuntimeBindings.GRAALVM,
    )
    cIgnores = listOf(
        "sock_shutdown-invalid_fd",
        "sock_shutdown-not_sock",
    )
    rustIgnores = listOf(
        "fd_fdstat_set_rights", // legacy, not used anywhere
        "overwrite_preopen",
        "poll_oneoff_stdio",
        "renumber",
        "symlink_filestat", // Fails on JVM because JVM rounds timestamps of symlinks to microseconds (JDK-8343417)
        "stdio",
        "unlink_file_trailing_slashes",
    )
}

kotlin {
    jvm()
    // TODO: iosSimulatorArm64(), iosArm64(), iosX64(), macosArm64(), macosX64(),
    linuxArm64()
    linuxX64()
    mingwX64()

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
            implementation(kotlin("test"))
        }
        jvmTest.dependencies {
            implementation(libs.chicory.wasi)
            implementation(projects.bindingsChicory)
            implementation(projects.bindingsGraalvm241)
        }
    }
}
