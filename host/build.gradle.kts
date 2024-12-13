/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.kotlin.dsl.withType
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
    propertiesFileKey = "weh_host_version",
    envVariableName = "WEH_HOST_VERSION",
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
    mingwX64 {
        setupWindowsInterops()
        binaries.all {
            linkerOpts("-lntdll")
        }
    }

    applyDefaultHierarchyTemplate()

    targets.withType<KotlinNativeTarget>().matching {
        when (it.konanTarget.family) {
            Family.OSX, Family.IOS, Family.TVOS, Family.WATCHOS -> true
            else -> false
        }
    }.configureEach {
        setupAppleInterops()
    }

    sourceSets {
        val appleAndLinuxMain by creating {
            dependsOn(nativeMain.get())
        }
        appleMain.get().dependsOn(appleAndLinuxMain)
        linuxMain.get().dependsOn(appleAndLinuxMain)

        commonMain.dependencies {
            api(projects.commonApi)
            api(libs.android.annotation) // TODO: remove
            api(libs.arrow.core)
            api(libs.kotlinx.io)
            implementation(libs.kotlinx.datetime)
            implementation(projects.commonUtil)
        }
        commonTest.dependencies {
            implementation(projects.hostTestFixtures)
            implementation(projects.testFilesystemAssertions)
            implementation(projects.testIgnoreAnnotations)
            implementation(projects.testIoBootstrap)
            implementation(projects.testLogger)
            implementation(projects.testTempfolder)
            implementation(kotlin("test"))
            implementation(libs.assertk)
        }
    }
}

private fun KotlinNativeTarget.setupLinuxInterops() = compilations.named("main") {
    cinterops {
        create("linux") {
            packageName("at.released.weh.host.platform.linux")
        }
    }
}

private fun KotlinNativeTarget.setupAppleInterops() {
    if (Os.isFamily("mac")) {
        compilations.named("main") {
            cinterops {
                create("apple") {
                    packageName("at.released.weh.host.platform.apple")
                }
            }
        }
    }
}

private fun KotlinNativeTarget.setupWindowsInterops() {
    compilations.named("main") {
        cinterops {
            create("windows") {
                packageName("at.released.weh.host.platform.windows")
            }
        }
    }
}
