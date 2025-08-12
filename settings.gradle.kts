pluginManagement {
    includeBuild("build-logic/settings")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("at.released.weh.gradle.settings.root")
}

// Workaround for https://github.com/gradle/gradle/issues/26020
buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.12.0")
        classpath("com.github.node-gradle:gradle-node-plugin:7.1.0")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.32.0")
        classpath("dev.drewhamilton.poko:poko-gradle-plugin:0.18.7")
        classpath("org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin:2.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.20")
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.1.21")
        classpath(
            "org.jetbrains.kotlinx.binary-compatibility-validator:" +
                    "org.jetbrains.kotlinx.binary-compatibility-validator.gradle.plugin:0.17.0",
        )
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.28.0")
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
include("test-wasi-testsuite:bindings-test")
include("wasm-core")
include("wasm-core-test-fixtures")
include("wasm-wasi-preview1-core")
include("wasm-wasi-preview1")
