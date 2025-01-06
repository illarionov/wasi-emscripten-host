plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(libs.chicory)
    implementation(libs.weh.chicory.emscripten)
}

application {
    mainClass = "at.released.weh.sample.chicory.gradle.app.AppKt"
}
