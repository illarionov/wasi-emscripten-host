/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Convention plugin that set up generation of Kotlin code from WASI WITX specifications
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        val extension = createTestGeneratorExtension()
        val testsTask = registerGenerateTestsTask(extension)
        sourceSets {
            commonTest {
                kotlin.srcDirs(testsTask.flatMap(GenerateWasiTestsuiteTestsTask::commonTestOutputDirectory))
            }
            matching { it.name == "jvmTest" }.configureEach {
                kotlin.srcDirs(testsTask.flatMap(GenerateWasiTestsuiteTestsTask::jvmTestOutputDirectory))
            }
        }
    }
}

fun registerGenerateTestsTask(
    extension: TestGeneratorExtension,
): Provider<GenerateWasiTestsuiteTestsTask> = tasks.register<GenerateWasiTestsuiteTestsTask>("generateWasi1Tests") {
    description = "Generates Kotlin Test wrappers for tests from WASI test suite"
    wasiTestsuiteTestsRoot = extension.wasiTestsuiteTestsRoot
    assemblyscriptIgnores = extension.assemblyscriptIgnores
    cIgnores = extension.cIgnores
    rustIgnores = extension.rustIgnores
    runtimes = extension.runtimes
}
