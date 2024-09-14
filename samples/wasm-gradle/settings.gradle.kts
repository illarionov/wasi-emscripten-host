plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

rootProject.name = "wasm-gradle"
include("app-graalvm")
include("app-chicory")
include("app-chasm")
