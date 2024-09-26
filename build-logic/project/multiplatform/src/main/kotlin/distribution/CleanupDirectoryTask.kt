/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.multiplatform.distribution

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class CleanupDirectoryTask : DefaultTask() {
    @get:InputFiles
    abstract val inputDirectory: DirectoryProperty

    @TaskAction
    fun cleanup() {
        val dir = inputDirectory.get().asFile
        if (dir.isDirectory) {
            dir.deleteRecursively()
        }
    }
}
