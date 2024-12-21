/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import at.released.weh.filesystem.ext.Os
import at.released.weh.filesystem.testutil.SymlinkType.SYMLINK_TO_DIRECTORY
import at.released.weh.test.ignore.annotations.dynamic.DynamicIgnoreTarget
import at.released.weh.test.ignore.annotations.dynamic.isCurrentSystem
import kotlinx.io.IOException
import kotlinx.io.files.Path
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import java.nio.file.Path as NioPath

internal actual fun createSymlink(oldPath: String, newPath: Path, type: SymlinkType) {
    val target = NioPath.of(oldPath)
    if (type == SYMLINK_TO_DIRECTORY && DynamicIgnoreTarget.JVM_ON_WINDOWS.isCurrentSystem()) {
        createDirectoryLinkOnWindows(target, newPath)
    } else {
        Files.createSymbolicLink(NioPath.of(newPath.toString()), target)
    }
}

private fun createDirectoryLinkOnWindows(
    target: NioPath,
    newPath: Path,
) {
    // If the target path does not exist, JVM NIO creates a symbolic link to a file on Windows instead of a directory.
    // To ensure the created link is of the correct type, we first create a temporary directory.
    val nioNewPath = NioPath.of(newPath.toString())
    val targetAbsolutePath = NioPath.of(newPath.toString()).toAbsolutePath().resolveSibling(target)
    if (targetAbsolutePath.exists()) {
        if (!targetAbsolutePath.isDirectory()) {
            throw IOException("Can not create directory link to non-directory")
        }
        Files.createSymbolicLink(nioNewPath, target)
    } else {
        val tempDirectory = targetAbsolutePath.createDirectories()
        try {
            Files.createSymbolicLink(nioNewPath, target)
        } finally {
            tempDirectory.deleteIfExists()
        }
    }
}

actual fun normalizeTargetPath(path: String): String {
    return if (Os.isWindows) {
        return path.replace('/', '\\')
    } else {
        return path
    }
}
