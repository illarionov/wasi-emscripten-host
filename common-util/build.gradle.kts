/*
* Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
* for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
* SPDX-License-Identifier: Apache-2.0
*/

@file:Suppress("OPT_IN_USAGE")

plugins {
    id("at.released.weh.gradle.lint.binary-compatibility-validator")
    id("at.released.weh.gradle.lint.android-lint-noagp")
    id("at.released.weh.gradle.multiplatform.kotlin")
    id("at.released.weh.gradle.multiplatform.publish")
}

group = "at.released.weh"
version = wehVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "weh_common_util_version",
    envVariableName = "WEH_COMMON_UTIL_VERSION",
).get()

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }
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
            api(libs.kotlinx.io)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertk)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
