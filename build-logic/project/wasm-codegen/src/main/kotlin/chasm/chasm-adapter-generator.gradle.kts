/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.chasm

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Gradle plugin that generates an adapter with WASI Preview1 functions for Chasm that proxies function calls
 * to common implementations
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        val moduleBuilderGeneratorTask = registerGenerateHostFunctionsBuilderTask()
        sourceSets.commonMain {
            kotlin.srcDirs(moduleBuilderGeneratorTask.flatMap(GenerateChasmHostFunctionsBuilderTask::outputDirectory))
        }
    }
}

fun registerGenerateHostFunctionsBuilderTask(): Provider<GenerateChasmHostFunctionsBuilderTask> =
    tasks.register<GenerateChasmHostFunctionsBuilderTask>("generateChasmHostFunctionsBuilder") {
        outputDirectory.set(layout.buildDirectory.dir("generated/chasm"))
    }
