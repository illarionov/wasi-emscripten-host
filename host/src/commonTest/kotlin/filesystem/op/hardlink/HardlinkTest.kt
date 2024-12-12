/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.hardlink

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.readFileContentToString
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import kotlin.test.Test

class HardlinkTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun hardlink_success_case() {
        tempFolder.createTestFile("testfile.txt", "Test content")
        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Hardlink,
                Hardlink(
                    oldBaseDirectory = tempFolderDirectoryFd,
                    oldPath = "testfile.txt".toVirtualPath(),
                    newBaseDirectory = tempFolderDirectoryFd,
                    newPath = "newfile.txt".toVirtualPath(),
                    followSymlinks = false,
                ),
            ).onLeft { error("Can not create hardlink: $it") }
        }

        val newFileContent = tempFolder.readFileContentToString("newfile.txt")

        assertThat(newFileContent).isEqualTo("Test content")
    }
}
