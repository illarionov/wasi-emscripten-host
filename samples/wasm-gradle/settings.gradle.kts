plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

rootProject.name = "wasm-gradle"
include("app-graalvm-emscripten")
include("app-chicory-emscripten")
include("app-chasm-emscripten")
