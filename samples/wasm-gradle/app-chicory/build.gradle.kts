plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(libs.chicory)
    implementation(libs.weh.chicory)
}

application {
    mainClass = "at.released.weh.sample.chicory.gradle.app.AppKt"
}
