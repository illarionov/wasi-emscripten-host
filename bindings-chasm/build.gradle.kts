/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.weh.gradle.lint.binary-compatibility-validator")
    id("at.released.weh.gradle.multiplatform.kotlin")
    id("at.released.weh.gradle.multiplatform.publish")
}

group = "at.released.weh"
version = wehVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "weh_bindings_chasm_version",
    envVariableName = "WEH_BINDINGS_CHASM_VERSION",
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
    mingwX64 {
        binaries.all {
            linkerOpts("-lole32")
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.host)
            api(libs.chasm)
            implementation(projects.commonApi)
            implementation(projects.commonUtil)
            implementation(libs.kotlinx.io)
            compileOnly(libs.chasm.memory)
            compileOnly(libs.chasm.runtime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertk)
        }
    }
}
