/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.graalvm

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Gradle plugin that generates WASI Preview1 functions for GraalVM that proxies function calls
 * to common implementations
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        val moduleBuilderGeneratorTask = registerGenerateHostFunctionsTask()
        sourceSets.jvmMain {
            kotlin.srcDirs(moduleBuilderGeneratorTask.flatMap(GenerateGraalvmHostFunctionsTask::outputDirectory))
        }
    }
}

fun registerGenerateHostFunctionsTask(): Provider<GenerateGraalvmHostFunctionsTask> =
    tasks.register<GenerateGraalvmHostFunctionsTask>("generateGraalvmHostFunctions") {
        outputDirectory.set(layout.buildDirectory.dir("generated/graalvm"))
    }
