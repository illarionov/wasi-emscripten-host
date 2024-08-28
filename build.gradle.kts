plugins {
    id("at.released.weh.gradle.lint.detekt")
    id("at.released.weh.gradle.lint.diktat")
    id("at.released.weh.gradle.lint.spotless")
}

tasks.register("styleCheck") {
    group = "Verification"
    description = "Runs code style checking tools (excluding tests)"
    dependsOn("detektCheck", "spotlessCheck", "diktatCheck")
}
