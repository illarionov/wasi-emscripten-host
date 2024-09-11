/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("GENERIC_VARIABLE_WRONG_DECLARATION")

package at.released.weh.gradle.documentation.docusaurus

import at.released.weh.gradle.documentation.docusaurus.BuildDocusaurusWebsiteTask.Companion.registerBuildWebsiteTask
import com.github.gradle.node.npm.task.NpmInstallTask

/*
 * Convention plugin responsible for building static website using Docusaurus
 */
plugins {
    id("com.github.node-gradle.node")
}

val websiteExtension = createDocusaurusWebsiteExtension()
val websiteNodePackageDir: Provider<Directory> = layout.buildDirectory.dir("docusaurus/nodePackage")

node {
    npmInstallCommand = "ci"
    nodeProjectDir = websiteNodePackageDir
}

val prepareNodePackageTask: TaskProvider<Sync> = tasks.register<Sync>("prepareNodePackage") {
    from(websiteExtension.websiteDirectory) {
        include("package-lock.json")
        include("package.json")
    }
    into(websiteNodePackageDir)
    preserve {
        include("node_modules")
        include(".docusaurus")
    }
}

val npmInstallTask: TaskProvider<NpmInstallTask> = tasks.named<NpmInstallTask>("npmInstall")
npmInstallTask.configure {
    dependsOn(prepareNodePackageTask)
}

registerBuildWebsiteTask(
    websiteDirectory = websiteExtension.websiteDirectory,
    outputDirectory = websiteExtension.outputDirectory,
).configure {
    dependsOn(npmInstallTask)
}
