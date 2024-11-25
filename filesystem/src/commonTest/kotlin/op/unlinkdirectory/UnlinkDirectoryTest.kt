/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.unlinkdirectory

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isIn
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.model.FileSystemErrno.NOENT
import at.released.weh.filesystem.model.FileSystemErrno.NOTDIR
import at.released.weh.filesystem.model.FileSystemErrno.NOTEMPTY
import at.released.weh.filesystem.op.unlink.UnlinkDirectory
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.SymlinkType.SYMLINK_TO_DIRECTORY
import at.released.weh.filesystem.testutil.SymlinkType.SYMLINK_TO_FILE
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.createTestSymlink
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.filesystem.assertions.isExists
import at.released.weh.test.filesystem.assertions.isNotExists
import at.released.weh.test.ignore.annotations.IgnoreApple
import at.released.weh.test.ignore.annotations.IgnoreLinux
import kotlin.test.Test
import kotlin.test.fail

class UnlinkDirectoryTest : BaseFileSystemIntegrationTest() {
    @Test
    fun unlinkdirectory_success_case() {
        val testFile = tempFolder.createTestDirectory()
        createTestFileSystem().use { fs ->
            val request = UnlinkDirectory(testFile.name, tempFolderDirectoryFd)
            fs.execute(UnlinkDirectory, request).getOrElse { fail("UnlinkDirectory failed: $it") }
        }
        assertThat(testFile).isNotExists()
    }

    @Test
    fun unlinkdirectory_on_file_should_fail() {
        val testDirectory = tempFolder.createTestFile()
        val unlinkError: UnlinkError? = createTestFileSystem().use { fs ->
            val request = UnlinkDirectory(testDirectory.name, tempFolderDirectoryFd)
            fs.execute(UnlinkDirectory, request).leftOrNull()
        }
        assertThat(unlinkError?.errno).isEqualTo(NOTDIR)
    }

    @Test
    fun unlinkdirectory_on_nonexistent_path_should_fail() {
        val unlinkError: UnlinkError? = createTestFileSystem().use { fs ->
            val request = UnlinkDirectory("nonexistent", tempFolderDirectoryFd)
            fs.execute(UnlinkDirectory, request).leftOrNull()
        }
        assertThat(unlinkError?.errno).isIn(NOENT, NOTDIR)
    }

    @Test
    @IgnoreLinux // TODO: fix
    @IgnoreApple // TODO: fix
    fun unlinkdirectory_on_symlink_to_directory_should_succeed() {
        val testDirectory = tempFolder.createTestDirectory()
        val testSymlink = tempFolder.createTestSymlink(testDirectory.name, "testSymlink", SYMLINK_TO_DIRECTORY)

        createTestFileSystem().use { fs ->
            val request = UnlinkDirectory(testSymlink.name, tempFolderDirectoryFd)
            fs.execute(UnlinkDirectory, request).getOrElse { fail("UnlinkDirectory failed: $it") }
        }
        assertThat(testDirectory).isExists()
        assertThat(testSymlink).isNotExists()
    }

    @Test
    fun unlinkdirectory_on_symlink_to_file_should_fail() {
        val testFile = tempFolder.createTestFile()
        val testSymlink = tempFolder.createTestSymlink(testFile.name, "testSymlink", SYMLINK_TO_FILE)

        val unlinkError: UnlinkError? = createTestFileSystem().use { fs ->
            val request = UnlinkDirectory(testSymlink.name, tempFolderDirectoryFd)
            fs.execute(UnlinkDirectory, request).leftOrNull()
        }
        assertThat(unlinkError?.errno).isEqualTo(NOTDIR)
    }

    @Test
    @IgnoreLinux // TODO: fix
    @IgnoreApple // TODO: fix
    fun unlinkdirectory_on_symlink_to_nonexistent_target_should_succeed() {
        val testSymlink = tempFolder.createTestSymlink("nonexistent_target", "testSymlink", SYMLINK_TO_DIRECTORY)

        createTestFileSystem().use { fs ->
            val request = UnlinkDirectory(testSymlink.name, tempFolderDirectoryFd)
            fs.execute(UnlinkDirectory, request).getOrElse { fail("UnlinkDirectory failed: $it") }
        }
        assertThat(testSymlink).isNotExists()
    }

    @Test
    fun unlinkdirectory_on_not_empty_directory_should_fail() {
        tempFolder.createTestDirectory("testdir")
        tempFolder.createTestFile("testdir/testfile.txt", size = 100)

        val unlinkError: UnlinkError? = createTestFileSystem().use { fs ->
            val request = UnlinkDirectory("testdir", tempFolderDirectoryFd)
            fs.execute(UnlinkDirectory, request).leftOrNull()
        }
        assertThat(unlinkError?.errno).isEqualTo(NOTEMPTY)
    }
}
