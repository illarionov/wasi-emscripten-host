plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(libs.weh.graalvm.wasip1)
    implementation(libs.graalvm.polyglot)
    implementation(libs.graalvm.wasm)
}

application {
    mainClass = "at.released.weh.sample.graalvm.gradle.app.AppKt"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
