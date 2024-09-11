/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.documentation.dokkatoo

/*
 * Base configuration of dokkatoo
 */
plugins {
    id("dev.adamko.dokkatoo-html")
}

dokkatoo {
    dokkatooPublications.configureEach {
        suppressObviousFunctions.set(true)
        suppressInheritedMembers.set(true)
    }

    dokkatooSourceSets.configureEach {
        includes.from(
            "MODULE.md",
        )
        sourceLink {
            localDirectory.set(project.layout.projectDirectory)
            val remoteUrlSubpath = project.path.replace(':', '/')
            remoteUrl("https://github.com/illarionov/wasi-emscripten-host/tree/main$remoteUrlSubpath")
        }
        externalDocumentationLinks {
            create("arrow") {
                url("https://apidocs.arrow-kt.io")
            }
        }
    }

    versions.apply {
        // XXX: named("libs") fails with "Catalog named libs doesn't exist" for some unknown reason,
        versionCatalogs.find("libs").ifPresent {
            jetbrainsDokka.set(it.findVersion("dokka").get().toString())
        }
    }

    pluginsConfiguration.html {
        templatesDir.set(rootProject.layout.projectDirectory.dir("aggregate-documentation-resources/templates"))
        homepageLink.set("https://weh.released.at")
        footerMessage.set("(C) wasi-emscripten-host project authors and contributors")
    }
}
