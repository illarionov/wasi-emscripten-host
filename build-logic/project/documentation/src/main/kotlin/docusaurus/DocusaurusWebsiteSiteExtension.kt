/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.documentation.docusaurus

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.kotlin.dsl.create

internal fun Project.createDocusaurusWebsiteExtension(): DocusaurusWebsiteSiteExtension {
    return extensions.create<DocusaurusWebsiteSiteExtension>("docusaurusWebsite").apply {
        @Suppress("UnstableApiUsage")
        websiteDirectory.convention(layout.settingsDirectory.dir("website"))
        outputDirectory.convention(layout.buildDirectory.dir("docusaurus/website"))
    }
}

public interface DocusaurusWebsiteSiteExtension {
    val websiteDirectory: DirectoryProperty
    val outputDirectory: DirectoryProperty
}
