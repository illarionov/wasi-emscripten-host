/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.documentation.docusaurus

import com.github.gradle.node.npm.task.NpmTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

public abstract class BuildDocusaurusWebsiteTask : NpmTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val websiteFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    companion object {
        fun Project.registerBuildWebsiteTask(
            websiteDirectory: Provider<Directory>,
            outputDirectory: Provider<Directory>,
            name: String = "buildDocusaurusWebsite",
        ): TaskProvider<BuildDocusaurusWebsiteTask> = tasks.register<BuildDocusaurusWebsiteTask>(name) {
            args.addAll("run", "build", "--")
            args.add(websiteDirectory.map { it.asFile.absolutePath })

            args.add("--out-dir")
            args.add(outputDirectory.map { it.asFile.absolutePath })

            websiteFiles.from(
                fileTree(websiteDirectory) {
                    exclude(
                        "node_modules",
                        ".docusaurus",
                        "build",
                    )
                },
            )
            this.outputDirectory.set(outputDirectory)

            doFirst {
                (this as BuildDocusaurusWebsiteTask).outputDirectory.get().asFile.apply {
                    mkdirs()
                    walkBottomUp()
                        .filter { it != this }
                        .onEach { it.delete() }
                }
            }
        }
    }
}
