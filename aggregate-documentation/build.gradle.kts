/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

import org.jetbrains.dokka.gradle.tasks.DokkaGeneratePublicationTask

/*
 * Module responsible for aggregating API reference from subprojects and creating final HTML documentation
 */
plugins {
    id("at.released.weh.gradle.documentation.dokka.base")
    id("at.released.weh.gradle.documentation.docusaurus.website")
}

group = "at.released.weh"

private val websiteOutputDirectory = layout.buildDirectory.dir("outputs/website")
private val apiReferenceDirectory = tasks.named<DokkaGeneratePublicationTask>("dokkaGeneratePublicationHtml")
    .flatMap(DokkaGeneratePublicationTask::outputDirectory)
private val docusaurusWebsiteDirectory = tasks.named("buildDocusaurusWebsite")

dokka {
    dokkaPublications.configureEach {
        moduleName.set("Wasi-emscripten-host")
        includes.from("FRONTPAGE.md")
    }
}

tasks.register<Sync>("buildWebsite") {
    description = "Assembles the final website from docusaurus output and api refernce into outputs/website"
    from(docusaurusWebsiteDirectory)
    from(apiReferenceDirectory) {
        into("api")
    }
    into(websiteOutputDirectory)
}

dependencies {
    dokka(projects.bindingsChasmEmscripten)
    dokka(projects.bindingsChasmWasip1)
    dokka(projects.bindingsChicoryEmscripten)
    dokka(projects.bindingsChicoryWasip1)
    dokka(projects.bindingsGraalvm241Emscripten)
    dokka(projects.bindingsGraalvm241Wasip1)
    dokka(projects.commonApi)
    dokka(projects.host)
    dokka(projects.hostTestFixtures)
    dokka(projects.testIoBootstrap)
    dokka(projects.testLogger)
    dokka(projects.wasmCore)
    dokka(projects.wasmWasiPreview1Core)
    dokka(projects.wasmWasiPreview1)
}
