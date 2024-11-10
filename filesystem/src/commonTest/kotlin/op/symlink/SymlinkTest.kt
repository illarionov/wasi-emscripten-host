/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.symlink

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.TEST_CONTENT
import at.released.weh.filesystem.testutil.TEST_FILE
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.readFileContentToString
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.ignore.annotations.IgnoreMingw
import kotlin.test.Test

@IgnoreMingw
class SymlinkTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun symlink_success_case() {
        tempFolder.createTestFile(TEST_FILE, TEST_CONTENT)

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Symlink,
                Symlink(TEST_FILE, "newfile.txt", newPathBaseDirectory = tempFolderDirectoryFd),
            ).onLeft { error("Can not create symlink: $it") }
        }

        val newFileContent = tempFolder.readFileContentToString("newfile.txt")

        assertThat(newFileContent).isEqualTo(TEST_CONTENT)
    }
}
