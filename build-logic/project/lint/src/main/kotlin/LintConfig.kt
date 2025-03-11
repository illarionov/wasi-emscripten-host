/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.lint

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.util.PatternFilterable

internal val Project.configRootDir: Directory
    get() {
        @Suppress("UnstableApiUsage")
        return layout.settingsDirectory.dir("config")
    }

@Suppress("UnstableApiUsage")
internal val Project.lintedFileTree: FileTree
    get() = layout.settingsDirectory.asFileTree.matching {
        excludeNonLintedDirectories()
    }

internal fun PatternFilterable.excludeNonLintedDirectories() {
    exclude {
        it.isDirectory && it.name in excludedDirectories
    }
    exclude {
        it.isDirectory && it.relativePath.startsWith("config/copyright")
    }
    exclude {
        it.isDirectory && it.relativePath.startsWith("test-wasi-testsuite/wasi-testsuite")
    }
    exclude("**/api/**/*.api")
}

private val excludedDirectories = setOf(
    ".git",
    ".gradle",
    ".idea",
    "build",
    "generated",
    "node_modules",
    "out",
)
