/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chicory

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Gradle plugin that generates an adapter with WASI Preview1 functions for Chasm that proxies function calls
 * to common implementations
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        val moduleBuilderGeneratorTask = registerGenerateHostFunctionsBuilderTask()
        sourceSets.jvmMain {
            kotlin.srcDirs(moduleBuilderGeneratorTask.flatMap(GenerateChicoryHostFunctionsBuilderTask::outputDirectory))
        }
    }
}

fun registerGenerateHostFunctionsBuilderTask(): Provider<GenerateChicoryHostFunctionsBuilderTask> =
    tasks.register<GenerateChicoryHostFunctionsBuilderTask>("generateChicoryHostFunctionsBuilder") {
        outputDirectory.set(layout.buildDirectory.dir("generated/chicory"))
    }
