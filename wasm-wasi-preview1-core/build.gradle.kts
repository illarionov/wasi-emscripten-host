/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.weh.gradle.lint.android-lint-noagp")
    id("at.released.weh.gradle.lint.binary-compatibility-validator")
    id("at.released.weh.gradle.multiplatform.kotlin")
    id("at.released.weh.gradle.multiplatform.publish")
    id("at.released.weh.gradle.wasm.codegen.wasitypes.witx-interface-generator")
}

group = "at.released.weh"
version = wehVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "weh_wasm_wasi_preview1_core_version",
    envVariableName = "WEH_WASM_WASI_PREVIEW1_CORE_VERSION",
).get()

kotlin {
    jvm()
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(projects.commonApi)
            api(projects.wasmCore)
        }
        commonTest.dependencies {
            implementation(projects.testIoBootstrap)
            implementation(projects.testLogger)
            implementation(kotlin("test"))
            implementation(libs.assertk)
            implementation(libs.tempfolder)
        }
    }
}
