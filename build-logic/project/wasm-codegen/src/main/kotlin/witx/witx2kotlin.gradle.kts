/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/*
 * Convention plugin that set up generation of Kotlin code from WASI WITX specifications
 */
plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<KotlinMultiplatformExtension> {
        val extension = createWitx2KotlinExtension()
        val typenamesTask = registerGenerateWasi1TypenamesTask(extension)
        sourceSets.commonMain {
            kotlin.srcDirs(typenamesTask.flatMap(GenerateWasi1InterfacesTask::outputDirectory))
        }
    }
}

fun registerGenerateWasi1TypenamesTask(
    extension: Witx2KotlinExtension,
): Provider<GenerateWasi1InterfacesTask> = tasks.register<GenerateWasi1InterfacesTask>("generateWasi1Interfaces") {
    typenamesSpec = extension.typenamesSpec
    typenamesPackage = extension.typenamesPackage
    outputDirectory = extension.outputDirectory
    functionsSpec = extension.functionsSpec
    functionsPackage = extension.functionsPackage
}
