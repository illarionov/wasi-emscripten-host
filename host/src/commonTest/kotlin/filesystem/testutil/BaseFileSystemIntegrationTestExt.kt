/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import at.released.tempfolder.sync.TempDirectory
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

internal const val TEST_DIRECTORY_NAME = "testdirectory"
internal const val TEST_FILE_NAME = "testfile.txt"
internal const val TEST_CONTENT = "Test Content"
internal const val TEST_LINK = "testlink"

internal val BaseFileSystemIntegrationTest.tempFolderDirectoryFd: DirectoryFd
    get() = DirectoryFd(WASI_FIRST_PREOPEN_FD)

internal fun TempDirectory<*>.path(
    relativePath: String = ".",
) = absolutePath().asString().let {
    if (relativePath != ".") {
        Path(it, relativePath)
    } else {
        Path(it)
    }
}

internal fun TempDirectory<*>.createTestFile(
    testfilePath: String = TEST_FILE_NAME,
    content: String = TEST_CONTENT,
): Path = createTestFile(testfilePath) { writeString(content) }

internal fun TempDirectory<*>.createTestFile(
    testfilePath: String = TEST_FILE_NAME,
    size: Int = 100,
    fillByte: Byte = 0xdd.toByte(),
): Path = createTestFile(testfilePath) { write(ByteArray(size) { fillByte }) }

internal fun TempDirectory<*>.createTestFile(
    testfilePath: String = TEST_FILE_NAME,
    content: Sink.() -> Unit,
): Path {
    val filePath = path(testfilePath)
    SystemFileSystem.run {
        sink(filePath).buffered().use(content)
    }
    return filePath
}

internal fun TempDirectory<*>.createTestDirectory(
    testDirectoryPath: String = TEST_DIRECTORY_NAME,
): Path {
    val path = path(testDirectoryPath)
    SystemFileSystem.createDirectories(path, mustCreate = true)
    return path
}

internal fun TempDirectory<*>.createTestSymlink(
    oldPath: String,
    newPath: String = TEST_LINK,
    type: SymlinkType = SymlinkType.NOT_SPECIFIED,
): Path {
    val newAbsolutePath = path(newPath)
    createSymlink(normalizeTargetPath(oldPath), newAbsolutePath, type)
    return newAbsolutePath
}

internal fun TempDirectory<*>.readFileContentToString(
    testfilePath: String = TEST_FILE_NAME,
): String = SystemFileSystem.source(path(testfilePath)).buffered().use {
    it.readString()
}
