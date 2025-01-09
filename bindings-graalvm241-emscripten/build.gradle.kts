/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.weh.gradle.lint.binary-compatibility-validator")
    id("at.released.weh.gradle.lint.android-lint-noagp")
    id("at.released.weh.gradle.multiplatform.kotlin")
    id("at.released.weh.gradle.multiplatform.publish")
}

group = "at.released.weh"
version = wehVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "weh_bindings_graalvm241_emscripten_version",
    envVariableName = "WEH_BINDINGS_GRAALVM241_EMSCRIPTEN_VERSION",
).get()

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.commonApi)
            api(projects.host)
            api(projects.emscriptenRuntime)
            api(projects.bindingsGraalvm241Wasip1)
            implementation(projects.commonApi)
            implementation(projects.commonUtil)
            implementation(libs.kotlinx.io)
        }
        jvmMain.dependencies {
            api(libs.graalvm241.polyglot.polyglot)
            compileOnly(libs.graalvm241.wasm.language)
            compileOnly(libs.graalvm241.polyglot.wasm)
            implementation(libs.graalvm241.truffle.api)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertk)
            implementation(libs.graalvm241.polyglot.wasm)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
