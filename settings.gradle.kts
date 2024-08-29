pluginManagement {
    includeBuild("build-logic/settings")
}

plugins {
    id("at.released.weh.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:7.0.0.BETA2")
        classpath("com.saveourtool.diktat:diktat-gradle-plugin:2.0.0")
        classpath("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.20")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.25.0")
    }
}

rootProject.name = "wasi-emscripten-host"

include("common-api")
include("common-util")
include("filesystem")
include("filesystem-test-fixtures")
include("host")
include("host-test-fixtures")
include("test-logger")
include("test-utils")
