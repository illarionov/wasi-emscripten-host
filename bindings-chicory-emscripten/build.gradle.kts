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
    propertiesFileKey = "weh_bindings_chicory_emscripten_version",
    envVariableName = "WEH_BINDINGS_CHICORY_EMSCRIPTEN_VERSION",
).get()

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.commonApi)
            api(projects.host)
            api(projects.bindingsChicoryWasip1)
            api(projects.emscriptenRuntime)
            api(libs.chicory.runtime)
            implementation(projects.commonUtil)
            implementation(projects.wasmWasiPreview1)
            implementation(libs.kotlinx.io)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertk)
        }
    }
}
