/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.test.utils.TempFolder
import at.released.weh.test.utils.absolutePath
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

internal const val TEST_DIRECTORY = "testdirectory"
internal const val TEST_FILE = "testfile.txt"
internal const val TEST_CONTENT = "Test Content"
internal const val TEST_LINK = "testlink"

internal val BaseFileSystemIntegrationTest.root: Path
    get() = tempFolder.absolutePath()

internal val BaseFileSystemIntegrationTest.tempFolderDirectoryFd: DirectoryFd
    get() = DirectoryFd(WASI_FIRST_PREOPEN_FD)

internal fun TempFolder.path(
    relativePath: String,
) = Path(absolutePath(), relativePath)

internal fun TempFolder.createTestFile(
    testfilePath: String = TEST_FILE,
    content: String = TEST_CONTENT,
): Path {
    val filePath = path(testfilePath)
    SystemFileSystem.run {
        sink(filePath).buffered().use {
            it.writeString(content)
        }
    }
    return filePath
}

internal fun TempFolder.createTestDirectory(
    testDirectoryPath: String = TEST_DIRECTORY,
): Path {
    val path = path(testDirectoryPath)
    SystemFileSystem.createDirectories(path, mustCreate = true)
    return path
}

internal fun TempFolder.createTestSymlink(
    oldPath: String,
    newPath: String = TEST_LINK,
): Path {
    val newAbsolutePath = path(newPath)
    createSymlink(oldPath, newAbsolutePath)
    return newAbsolutePath
}

internal fun TempFolder.readFileContentToString(
    testfilePath: String = TEST_FILE,
): String = SystemFileSystem.source(path(testfilePath)).buffered().use {
    it.readString()
}
