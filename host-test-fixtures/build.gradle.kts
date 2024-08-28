/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.weh.gradle.multiplatform.kotlin")
}

group = "at.released.weh"

kotlin {
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    jvm()
    linuxArm64()
    linuxX64()
    macosArm64()
    macosX64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            api(projects.host)
            api(projects.testUtils)
            api(projects.filesystemTestFixtures)
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.datetime)
        }
    }
}
