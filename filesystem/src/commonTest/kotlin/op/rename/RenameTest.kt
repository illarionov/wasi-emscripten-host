/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.rename

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.ISDIR
import at.released.weh.filesystem.op.fdattributes.FdAttributes
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.TEST_CONTENT
import at.released.weh.filesystem.testutil.TEST_FILE_NAME
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.createTestSymlink
import at.released.weh.filesystem.testutil.path
import at.released.weh.filesystem.testutil.readFileContentToString
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.filesystem.assertions.isDirectory
import at.released.weh.test.filesystem.assertions.isExists
import at.released.weh.test.filesystem.assertions.isNotExists
import at.released.weh.test.filesystem.assertions.isRegularFile
import at.released.weh.test.ignore.annotations.IgnoreMingw
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.fail

@IgnoreMingw
class RenameTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun rename_file_success_case() {
        val testFilePath = tempFolder.createTestFile(TEST_FILE_NAME, TEST_CONTENT)

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, TEST_FILE_NAME, tempFolderDirectoryFd, "newfile.txt"),
            ).onLeft { fail("Can not rename file: $it") }
        }

        val newFileContent = tempFolder.readFileContentToString("newfile.txt")

        assertThat(newFileContent).isEqualTo(TEST_CONTENT)
        assertThat(testFilePath).isNotExists()
    }

    @Test
    public fun rename_file_to_itself_should_do_nothing() {
        val testFilePath = tempFolder.createTestFile(TEST_FILE_NAME, TEST_CONTENT)

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, testFilePath.name, tempFolderDirectoryFd, testFilePath.name),
            ).onLeft { fail("Can not rename file: $it") }
        }

        // XXX: need check that last modified is not changing
        assertThat(testFilePath).isExists()
    }

    @Test
    public fun rename_file_to_existing_file_should_rewrite_file() {
        val srcFilePath = tempFolder.createTestFile()
        val dstFilePath = tempFolder.createTestFile("dstFile.txt", "To be removed content")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcFilePath.name, tempFolderDirectoryFd, dstFilePath.name),
            ).onLeft { fail("Can not rename file: $it") }
        }

        val dstFileContent = tempFolder.readFileContentToString(dstFilePath.name)

        assertThat(dstFileContent).isEqualTo(TEST_CONTENT)
        assertThat(srcFilePath).isNotExists()
    }

    @Test
    public fun rename_file_to_existing_directory_should_fail() {
        val testFilePath = tempFolder.createTestFile()
        val testDstDirectory = tempFolder.createTestDirectory("testDirectory")

        val errNo = createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, testFilePath.name, tempFolderDirectoryFd, testDstDirectory.name),
            ).fold(ifLeft = { it.errno }, ifRight = { FileSystemErrno.SUCCESS })
        }

        assertThat(errNo).isEqualTo(ISDIR)
    }

    @Test
    public fun renamed_opened_file_should_be_readable() {
        val testFilePath = tempFolder.createTestFile()
        val testDstFile = tempFolder.path("dstfile.txt")

        createTestFileSystem().use { fileSystem ->
            val openedTestFile = fileSystem.execute(
                Open,
                Open(testFilePath.name, tempFolderDirectoryFd, OpenFileFlag.O_RDONLY, 0),
            ).getOrElse { fail("Can not open test file") }

            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, testFilePath.name, tempFolderDirectoryFd, testDstFile.name),
            ).onLeft { fail("Can not rename file: $it") }

            val bbuf = FileSystemByteBuffer(ByteArray(100))

            val readBytes = fileSystem.execute(
                ReadFd,
                ReadFd(openedTestFile, listOf(bbuf), ReadWriteStrategy.CurrentPosition),
            ).getOrElse { fail("Can not read renamed file: $it") }
            val encodedTestContent: Int = TEST_CONTENT.encodeToByteArray().size

            assertThat(testFilePath).isNotExists()
            assertThat(readBytes.toInt()).isEqualTo(encodedTestContent)

            fileSystem.execute(
                FdAttributes,
                FdAttributes(openedTestFile),
            ).onLeft { fail("Can not get attributes of renamed file: $it") }
        }
    }

    @Test
    public fun rename_directory_success_case() {
        val srcDirectory = tempFolder.createTestDirectory("testdirectory")
        val dstDirectory = tempFolder.path("dstDirectory")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcDirectory.name, tempFolderDirectoryFd, dstDirectory.name),
            ).onLeft { fail("Can not rename directory: $it") }
        }

        assertThat(dstDirectory).isDirectory()
        assertThat(srcDirectory).isNotExists()
    }

    @Test
    public fun rename_nonempty_directory_may_succeed() {
        val srcDirectory = tempFolder.createTestDirectory("testdirectory")
        tempFolder.createTestFile("testdirectory/testfile.txt")
        val dstDirectory = tempFolder.path("dstDirectory")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcDirectory.name, tempFolderDirectoryFd, dstDirectory.name),
            )
        }.onLeft { fail("Can not rename non-empty directory: $it") }

        assertThat(dstDirectory).isDirectory()
        assertThat(Path(dstDirectory, "testfile.txt")).isRegularFile()
    }

    @Test
    public fun rename_directory_to_empty_directory_should_succeed() {
        val srcDirectory = tempFolder.createTestDirectory("testdirectory")
        tempFolder.createTestFile("testdirectory/testfile.txt")
        val dstDirectory = tempFolder.createTestDirectory("dstDirectory")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcDirectory.name, tempFolderDirectoryFd, dstDirectory.name),
            )
        }.onLeft { fail("Can not rename directory to empty directory directory: $it") }

        assertThat(dstDirectory).isDirectory()
        assertThat(Path(dstDirectory, "testfile.txt")).isRegularFile()
    }

    @Test
    public fun rename_directory_to_nonempty_directory_should_fail() {
        val srcDirectory = tempFolder.createTestDirectory("testdirectory")
        val dstDirectory = tempFolder.createTestDirectory("dstDirectory")
        tempFolder.createTestFile("dstDirectory/testfile.txt")

        val errno = createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcDirectory.name, tempFolderDirectoryFd, dstDirectory.name),
            )
        }.fold(ifLeft = { it.errno }, ifRight = { FileSystemErrno.SUCCESS })

        assertThat(errno).isEqualTo(FileSystemErrno.NOTEMPTY)
    }

    @Test
    public fun rename_directory_to_file_should_fail() {
        val srcDirectory = tempFolder.createTestDirectory("testdirectory")
        val dstFile = tempFolder.createTestFile("dstFile.txt")

        val errno = createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcDirectory.name, tempFolderDirectoryFd, dstFile.name),
            )
        }.fold(ifLeft = { it.errno }, ifRight = { FileSystemErrno.SUCCESS })

        assertThat(errno).isEqualTo(FileSystemErrno.NOTDIR)
    }

    @Test
    public fun opened_renamed_directory_should_be_readable_after_renaming() {
        val srcDirectory = tempFolder.createTestDirectory("srcDirectory")
        val dstDirectory = tempFolder.path("dstDirectory")
        tempFolder.createTestFile("srcDirectory/testfile.txt")

        createTestFileSystem().use { fileSystem ->
            val openedSrcDirectoryFd = fileSystem.execute(
                Open,
                Open(srcDirectory.name, tempFolderDirectoryFd, OpenFileFlag.O_DIRECTORY, 0),
            ).getOrElse { fail("Can not open source directory") }

            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcDirectory.name, tempFolderDirectoryFd, dstDirectory.name),
            ).getOrElse { fail("Can not rename source directory") }

            val testFilestat: StructStat = fileSystem.execute(
                Stat,
                Stat("testfile.txt", DirectoryFd(openedSrcDirectoryFd)),
            ).getOrElse { fail("Can not get attributes of test file in source directory") }

            assertThat(testFilestat.size).isEqualTo(TEST_CONTENT.encodeToByteArray().size.toLong())
        }
    }

    @Test
    public fun rename_symlink_success_case() {
        val targetFile = tempFolder.createTestFile("target.txt")
        val srcSymlink = tempFolder.createTestSymlink(targetFile.name, "testlink")
        val dstSymlink = tempFolder.path("dstLink")

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Rename,
                Rename(tempFolderDirectoryFd, srcSymlink.name, tempFolderDirectoryFd, dstSymlink.name),
            ).onLeft { fail("Can not rename directory: $it") }
        }

        assertThat(targetFile).isExists()
        val fileContent = tempFolder.readFileContentToString(targetFile.name)

        assertThat(fileContent).isEqualTo(TEST_CONTENT)
        assertThat(srcSymlink).isNotExists()
        assertThat(dstSymlink).isExists() // XXX should be symlink
    }
}
