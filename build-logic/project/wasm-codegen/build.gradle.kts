/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    antlr
    `kotlin-dsl`
}

group = "at.released.weh.gradle.wasm.codegen"

configurations {
    // Exclude antlr4 from transitive dependencies (https://github.com/gradle/gradle/issues/820)
    api {
        setExtendsFrom(extendsFrom.filterNot { it == antlr.get() })
    }
}

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")
val generateGrammarSourceTask: TaskProvider<AntlrTask> = tasks.named<AntlrTask>("generateGrammarSource")

sourceSets {
    main {
        java.srcDir(generateGrammarSourceTask.map { it.outputDirectory })
    }

    test {
        java.srcDirs(
            generateGrammarSourceTask.map { it.outputDirectory },
        )
    }
}

val witxPackage = "at.released.weh.gradle.wasm.codegen.antlr"
val witxPackageSubdirectory = witxPackage.replace('.', '/')
tasks.withType<AntlrTask>().configureEach {
    inputs.property("witxPackageSubdirectory", witxPackageSubdirectory)
    arguments = arguments + listOf(
        "-lib", "build/generated-src/antlr/main/$witxPackageSubdirectory",
        "-package", witxPackage,
        "-no-listener",
    )
    doFirst {
        val witxSubdirectory = this.inputs.properties["witxPackageSubdirectory"]!!.toString()
        val outputDirectory = (this as AntlrTask).outputDirectory
        File(outputDirectory, witxSubdirectory).mkdirs()
    }
}

dependencies {
    antlr(libs.antlr4)
    implementation(libs.antlr4.runtime)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlinpoet)
}
