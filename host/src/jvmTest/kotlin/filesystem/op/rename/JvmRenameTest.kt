/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.rename

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import kotlin.test.Test
import kotlin.test.fail

public class JvmRenameTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun overwriting_open_file_should_fail() {
        val srcFile = tempFolder.createTestFile()
        val dstFile = tempFolder.createTestFile("dstfile.txt", "dstContent")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Open,
                Open(srcFile.name.toVirtualPath(), tempFolderDirectoryFd, OpenFileFlag.O_RDONLY, 0),
            ).getOrElse { fail("Can not open source file") }
            fileSystem.execute(
                Open,
                Open(dstFile.name.toVirtualPath(), tempFolderDirectoryFd, OpenFileFlag.O_RDONLY, 0),
            ).getOrElse { fail("Can not open destination file") }

            val errno = fileSystem.execute(
                Rename,
                Rename(
                    tempFolderDirectoryFd,
                    srcFile.name.toVirtualPath(),
                    tempFolderDirectoryFd,
                    dstFile.name.toVirtualPath(),
                ),
            ).fold(ifLeft = { it.errno }, ifRight = { FileSystemErrno.SUCCESS })

            assertThat(errno).isEqualTo(FileSystemErrno.PERM)
        }
    }

    @Test
    public fun overwriting_open_directory_should_fail() {
        val srcDirectory = tempFolder.createTestDirectory("srcDirectory")
        val dstDirectory = tempFolder.createTestDirectory("dstDirectory")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Open,
                Open(dstDirectory.name.toVirtualPath(), tempFolderDirectoryFd, OpenFileFlag.O_DIRECTORY, 0),
            ).getOrElse { fail("Can not open destination directory") }

            val errno = fileSystem.execute(
                Rename,
                Rename(
                    tempFolderDirectoryFd,
                    srcDirectory.name.toVirtualPath(),
                    tempFolderDirectoryFd,
                    dstDirectory.name.toVirtualPath(),
                ),
            ).fold(ifLeft = { it.errno }, ifRight = { FileSystemErrno.SUCCESS })

            assertThat(errno).isEqualTo(FileSystemErrno.PERM)
        }
    }
}
