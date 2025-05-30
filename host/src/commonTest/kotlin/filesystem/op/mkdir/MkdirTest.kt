/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.mkdir

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.FileModeFlag
import at.released.weh.filesystem.model.FileSystemErrno.EXIST
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.createTestSymlink
import at.released.weh.filesystem.testutil.path
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.filesystem.assertions.isDirectory
import at.released.weh.test.filesystem.assertions.mode.PosixFileModeBit.USER_EXECUTE
import at.released.weh.test.filesystem.assertions.mode.PosixFileModeBit.USER_READ
import at.released.weh.test.filesystem.assertions.mode.PosixFileModeBit.USER_WRITE
import at.released.weh.test.filesystem.assertions.mode.posixFileModeIfSupportedIsEqualTo
import kotlin.test.Test
import kotlin.test.fail

class MkdirTest : BaseFileSystemIntegrationTest() {
    @Test
    fun mkdir_success_case() {
        val testdirPath = tempFolder.path("testdir")

        createTestFileSystem().use { fs ->
            val request = Mkdir(
                testdirPath.name.toVirtualPath(),
                tempFolderDirectoryFd,
                FileModeFlag.S_IRWXU,
            )
            fs.execute(Mkdir, request).getOrElse { fail("mkdir failed: $it") }
        }

        assertThat(testdirPath).isDirectory()
        assertThat(testdirPath).posixFileModeIfSupportedIsEqualTo(USER_WRITE, USER_READ, USER_EXECUTE)
    }

    @Test
    fun mkdir_should_not_fail_if_directory_exists() {
        val testDir = tempFolder.createTestDirectory()
        createTestFileSystem().use { fs ->
            val request = Mkdir(
                path = testDir.name.toVirtualPath(),
                baseDirectory = tempFolderDirectoryFd,
                mode = FileModeFlag.S_IRWXU,
                failIfExists = false,
            )
            fs.execute(Mkdir, request).getOrElse { fail("mkdir failed: $it") }
        }
    }

    @Test
    fun mkdir_should_fail_if_exists_file() {
        val testDir = tempFolder.createTestFile()
        val error = createTestFileSystem().use { fs ->
            val request = Mkdir(
                path = testDir.name.toVirtualPath(),
                baseDirectory = tempFolderDirectoryFd,
                mode = FileModeFlag.S_IRWXU,
                failIfExists = true,
            )
            fs.execute(Mkdir, request).leftOrNull()
        }
        assertThat(error?.errno).isEqualTo(EXIST)
    }

    @Test
    fun mkdir_should_fail_if_exists_symlink() {
        tempFolder.createTestDirectory("dir2")
        tempFolder.createTestSymlink("dir2", "symlink")

        val error = createTestFileSystem().use { fs ->
            val request = Mkdir(
                path = "symlink".toVirtualPath(),
                baseDirectory = tempFolderDirectoryFd,
                mode = FileModeFlag.S_IRWXU,
                failIfExists = true,
            )
            fs.execute(Mkdir, request).leftOrNull()
        }
        assertThat(error?.errno).isEqualTo(EXIST)
    }

    @Test
    fun mkdir_should_fail_if_directory_exists_and_required_to_fail() {
        val testDir = tempFolder.createTestDirectory()
        val error = createTestFileSystem().use { fs ->
            val request = Mkdir(
                path = testDir.name.toVirtualPath(),
                baseDirectory = tempFolderDirectoryFd,
                mode = FileModeFlag.S_IRWXU,
                failIfExists = true,
            )
            fs.execute(Mkdir, request).leftOrNull()
        }
        assertThat(error?.errno).isEqualTo(EXIST)
    }
}
