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
//        WasmRuntimeBindings.CHICORY,
//        WasmRuntimeBindings.GRAALVM,
    )
    cIgnores = listOf(
        "fdopendir-with-access",
        "lseek",
        "pread-with-access",
        "pwrite-with-access",
        "pwrite-with-append",
        "sock_shutdown-invalid_fd",
        "sock_shutdown-not_sock",
        "stat-dev-ino",
    )
    rustIgnores = listOf(
        "close_preopen",
        "dangling_fd",
        "dangling_symlink",
        "dir_fd_op_failures",
        "directory_seek",
        "fd_advise",
        "fd_fdstat_set_rights",
        "fd_filestat_set",
        "fd_flags_set",
        "fd_readdir",
        "file_allocate",
        "file_pread_pwrite",
        "file_seek_tell",
        "file_truncation",
        "file_unbuffered_write",
        "fstflags_validate",
        "interesting_paths",
        "isatty",
        "nofollow_errors",
        "overwrite_preopen",
        "path_exists",
        "path_filestat",
        "path_link",
        "path_open_create_existing",
        "path_open_dirfd_not_dir",
        "path_open_missing",
        "path_open_nonblock",
        "path_open_read_write",
        "path_rename",
        "path_rename_dir_trailing_slashes",
        "path_symlink_trailing_slashes",
        "poll_oneoff_stdio",
        "readlink",
        "remove_directory_trailing_slashes",
        "remove_nonempty_directory",
        "renumber",
        "stdio",
        "symlink_create",
        "symlink_filestat",
        "symlink_loop",
        "truncation_rights",
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
