/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.symlink

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.TEST_CONTENT
import at.released.weh.filesystem.testutil.TEST_FILE_NAME
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.readFileContentToString
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import kotlin.test.Test

class SymlinkTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun symlink_success_case() {
        tempFolder.createTestFile(TEST_FILE_NAME, TEST_CONTENT)

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Symlink,
                Symlink(
                    TEST_FILE_NAME.toVirtualPath(),
                    "newfile.txt".toVirtualPath(),
                    newPathBaseDirectory = tempFolderDirectoryFd,
                ),
            ).onLeft { error("Can not create symlink: $it") }
        }

        val newFileContent = tempFolder.readFileContentToString("newfile.txt")

        assertThat(newFileContent).isEqualTo(TEST_CONTENT)
    }

    @Test
    fun symlink_into_subdirectory_success_case() {
        tempFolder.createTestDirectory("subdir")
        tempFolder.createTestFile("subdir/testfile", TEST_CONTENT)

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Symlink,
                Symlink(
                    "subdir/testfile".toVirtualPath(),
                    "newfile.txt".toVirtualPath(),
                    newPathBaseDirectory = tempFolderDirectoryFd,
                ),
            ).onLeft { error("Can not create symlink: $it") }
        }

        val newFileContent = tempFolder.readFileContentToString("newfile.txt")

        assertThat(newFileContent).isEqualTo(TEST_CONTENT)
    }
}
