/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

/*
 * Module responsible for aggregating API reference from subprojects and creating final HTML documentation
 */
plugins {
    id("at.released.weh.gradle.documentation.dokkatoo.aggregate")
    id("at.released.weh.gradle.documentation.docusaurus.website")
}

group = "at.released.weh"

private val websiteOutputDirectory = layout.buildDirectory.dir("outputs/website")
private val apiReferenceDirectory = tasks.named("dokkatooGeneratePublicationHtml")
private val docusaurusWebsiteDirectory = tasks.named("buildDocusaurusWebsite")

tasks.register<Sync>("buildWebsite") {
    from(docusaurusWebsiteDirectory)
    from(apiReferenceDirectory) {
        into("api")
    }
    into(websiteOutputDirectory)
}

dependencies {
    dokkatoo(projects.bindingsChasm)
    dokkatoo(projects.bindingsChicory)
    dokkatoo(projects.bindingsGraalvm240)
    dokkatoo(projects.commonApi)
    dokkatoo(projects.filesystem)
    dokkatoo(projects.filesystemTestFixtures)
    dokkatoo(projects.host)
    dokkatoo(projects.hostTestFixtures)
    dokkatoo(projects.testIoBootstrap)
    dokkatoo(projects.testLogger)
}