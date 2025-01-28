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
        classpath("com.android.tools.build:gradle:8.7.3")
        classpath("com.diffplug.spotless:spotless-plugin-gradle:7.0.0")
        classpath("com.github.node-gradle:gradle-node-plugin:7.0.2")
        classpath("com.saveourtool.diktat:diktat-gradle-plugin:2.0.0")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.30.0")
        classpath("dev.drewhamilton.poko:poko-gradle-plugin:0.18.2")
        classpath("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.7")
        classpath("org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin:2.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.1.0")
        classpath(
            "org.jetbrains.kotlinx.binary-compatibility-validator:" +
                    "org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:0.16.3",
        )
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.26.1")
    }
}

rootProject.name = "wasi-emscripten-host"

include("aggregate-documentation")
include("aggregate-distribution")
include("bindings-chasm-emscripten")
include("bindings-chasm-wasip1")
include("bindings-chicory-emscripten")
include("bindings-chicory-wasip1")
include("bindings-graalvm241-emscripten")
include("bindings-graalvm241-wasip1")
include("common-api")
include("common-util")
include("emscripten-runtime")
include("host")
include("host-test-fixtures")
include("test-filesystem-assertions")
include("test-ignore-annotations")
include("test-io-bootstrap")
include("test-logger")
include("test-tempfolder")
include("test-wasi-testsuite:bindings-test")
include("wasm-core")
include("wasm-core-test-fixtures")
include("wasm-wasi-preview1-core")
include("wasm-wasi-preview1")
