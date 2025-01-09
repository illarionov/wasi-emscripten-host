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
include("app-graalvm-emscripten")
include("app-graalvm-wasip1")
include("app-chicory-emscripten")
include("app-chicory-wasip1")
include("app-chasm-emscripten")
include("app-chasm-wasip1")
