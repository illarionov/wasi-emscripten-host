/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family

plugins {
    id("at.released.weh.gradle.multiplatform.atomicfu")
    id("at.released.weh.gradle.lint.binary-compatibility-validator")
    id("at.released.weh.gradle.lint.android-lint-noagp")
    id("at.released.weh.gradle.multiplatform.kotlin")
    id("at.released.weh.gradle.multiplatform.publish")
}

group = "at.released.weh"
version = wehVersions.getSubmoduleVersionProvider(
    propertiesFileKey = "weh_filesystem_version",
    envVariableName = "WEH_FILESYSTEM_VERSION",
).get()

kotlin {
    jvm()
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    linuxArm64 {
        setupLinuxInterops()
    }
    linuxX64 {
        setupLinuxInterops()
    }
    macosArm64()
    macosX64()
    mingwX64()

    targets.withType<KotlinNativeTarget>().matching {
        when (it.konanTarget.family) {
            Family.OSX, Family.IOS, Family.TVOS, Family.WATCHOS -> true
            else -> false
        }
    }.configureEach {
        setupAppleInterops()
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.commonApi)
            api(libs.android.annotation)
            api(libs.arrow.core)
            api(libs.kotlinx.io)
            implementation(projects.commonUtil)
            implementation(projects.testLogger)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(projects.filesystemTestFixtures)
            implementation(projects.testFilesystemAssertions)
            implementation(projects.testIgnoreAnnotations)
            implementation(projects.testTempfolder)
            implementation(libs.assertk)
        }
    }
}

tasks.matching { it.name.endsWith("linuxArm64MetadataElements") }.configureEach {
    mustRunAfter(tasks.named("commonizeCInterop"))
}

fun KotlinNativeTarget.setupLinuxInterops() = compilations.named("main") {
    cinterops {
        create("atfile") {
            packageName("at.released.weh.filesystem.platform.linux")
        }
    }
}

fun KotlinNativeTarget.setupAppleInterops() = compilations.named("main") {
    cinterops {
        create("apple") {
            packageName("at.released.weh.filesystem.platform.apple")
        }
    }
}
