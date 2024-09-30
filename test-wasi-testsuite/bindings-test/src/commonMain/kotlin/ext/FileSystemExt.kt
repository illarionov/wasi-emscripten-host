/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.ext

import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path

internal fun FileSystem.isRegularFile(path: Path) = metadataOrNull(path)?.isRegularFile ?: false

internal fun FileSystem.isDirectory(path: Path) = metadataOrNull(path)?.isDirectory ?: false

internal expect fun FileSystem.setCurrentWorkingDirectory(path: Path)

internal fun FileSystem.copyRecursively(
    srcDir: Path,
    dstDir: Path,
) {
    val srcAbsolutePath = resolve(srcDir).toString()
    walkTopDown(srcDir)
        .forEach { path ->
            val srcPath = resolve(path).toString()
            val isRoot = srcPath == srcAbsolutePath

            if (!srcPath.startsWith(srcAbsolutePath)) {
                error("Unexpected src path: `$path`")
            }
            val srcRelativePath = srcPath.removePrefix(srcAbsolutePath)
            val dstPath = Path(dstDir, srcRelativePath)

            when {
                this.isDirectory(path) -> this.createDirectories(dstPath, mustCreate = !isRoot)

                this.isRegularFile(path) -> this.source(path).buffered().use { source ->
                    this.sink(dstPath).use { sink ->
                        source.transferTo(sink)
                    }
                }

                else -> error("Not a directory or a file")
            }
        }
}
