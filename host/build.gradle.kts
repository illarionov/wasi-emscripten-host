/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("at.released.weh.gradle.multiplatform.atomicfu")
    id("at.released.weh.gradle.lint.binary-compatibility-validator")
    id("at.released.weh.gradle.lint.android-lint-noagp")
    id("at.released.weh.gradle.multiplatform.kotlin")
    id("at.released.weh.gradle.multiplatform.publish")
}

group = "at.released.weh"
version = wehVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "weh_host_version",
    envVariableName = "WEH_HOST_VERSION",
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
            api(projects.filesystem)
            implementation(libs.kotlinx.datetime)
            implementation(projects.commonUtil)
        }
        commonTest.dependencies {
            implementation(projects.filesystemTestFixtures)
            implementation(projects.hostTestFixtures)
            implementation(projects.testFilesystemAssertions)
            implementation(projects.testIoBootstrap)
            implementation(projects.testLogger)
            implementation(projects.testTempfolder)
            implementation(kotlin("test"))
            implementation(libs.assertk)
        }
    }
}
