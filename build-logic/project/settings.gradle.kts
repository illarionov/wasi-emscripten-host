import at.released.weh.gradle.settings.repository.googleFiltered

/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

pluginManagement {
    includeBuild("../settings")
}

plugins {
    id("at.released.weh.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$embeddedKotlinVersion")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../gradle/libs.versions.toml"))
        }
    }
    repositories {
        googleFiltered()
        mavenCentral()
        gradlePluginPortal()
    }
}

include("documentation")
include("lint")
include("multiplatform")

rootProject.name = "weh-gradle-project-plugins"
