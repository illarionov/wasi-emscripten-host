/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.unlinkfile

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.model.FileSystemErrno.ISDIR
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.SymlinkType.SYMLINK_TO_DIRECTORY
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.createTestSymlink
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.filesystem.assertions.isExists
import at.released.weh.test.filesystem.assertions.isNotExists
import at.released.weh.test.ignore.annotations.IgnoreApple
import kotlin.test.Test
import kotlin.test.fail

class UnlinkFileTest : BaseFileSystemIntegrationTest() {
    @Test
    fun unlinkfile_success_case() {
        val testFile = tempFolder.createTestFile(size = 100)
        createTestFileSystem().use { fs ->
            val request = UnlinkFile(testFile.name.toVirtualPath(), tempFolderDirectoryFd)
            fs.execute(UnlinkFile, request).getOrElse { fail("UnlinkFile failed: $it") }
        }
        assertThat(testFile).isNotExists()
    }

    @Test
    @IgnoreApple // TODO: fix
    fun unlinkfile_on_directory_should_fail() {
        val testDirectory = tempFolder.createTestDirectory()
        val unlinkError: UnlinkError? = createTestFileSystem().use { fs ->
            val request = UnlinkFile(testDirectory.name.toVirtualPath(), tempFolderDirectoryFd)
            fs.execute(UnlinkFile, request).leftOrNull()
        }
        assertThat(unlinkError?.errno).isEqualTo(ISDIR)
    }

    @Test
    fun unlinkfile_symlink_success_case() {
        val testFile = tempFolder.createTestFile(size = 100)
        val testSymlink = tempFolder.createTestSymlink(testFile.name, "testSymlink")

        createTestFileSystem().use { fs ->
            val request = UnlinkFile(testSymlink.name.toVirtualPath(), tempFolderDirectoryFd)
            fs.execute(UnlinkFile, request).getOrElse { fail("UnlinkFile failed for symlink: $it") }
        }
        assertThat(testFile).isExists()
        assertThat(testSymlink).isNotExists()
    }

    @Test
    fun unlinkfile_on_symlink_to_directory_should_success() {
        val testSymlink = tempFolder.createTestSymlink("testDirectory", "testSymlink", SYMLINK_TO_DIRECTORY)

        createTestFileSystem().use { fs ->
            val request = UnlinkFile(testSymlink.name.toVirtualPath(), tempFolderDirectoryFd)
            fs.execute(UnlinkFile, request).getOrElse { fail("UnlinkFile failed for symlink: $it") }
        }
        assertThat(testSymlink).isNotExists()
    }
}
