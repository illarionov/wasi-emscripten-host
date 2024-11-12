/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.fallocate

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Whence.CUR
import at.released.weh.filesystem.model.Whence.SET
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.test.filesystem.assertions.fileSize
import at.released.weh.test.ignore.annotations.IgnoreMingw
import at.released.weh.test.utils.absolutePath
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.fail

@IgnoreMingw
class FallocateTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun fallocate_success_case_from_middle_of_file_with_extend() {
        val testFile = createTestFile(size = 100)

        createTestFileSystem().use { fs ->
            val fd = fs.execute(Open, crestOpenFileCommand(testFile))
                .getOrElse { fail("Can not open test file") }

            fs.execute(SeekFd, SeekFd(fd, 50, SET)).onLeft { fail("Can not set new position") }

            fs.execute(FallocateFd, FallocateFd(fd, 90, 110)).onLeft { fail("Can not fallocate") }

            val position = fs.execute(SeekFd, SeekFd(fd, 0, CUR))
                .getOrElse { fail("Can not get current position") }

            assertThat(position).isEqualTo(50)
        }

        assertThat(testFile).fileSize().isEqualTo(200)

        val oldContent: ByteArray = SystemFileSystem.source(testFile).buffered().use {
            it.readByteArray(100)
        }

        assertThat(oldContent.all { it == 0xdd.toByte() }).isTrue()
    }

    @Test
    public fun fallocate_in_file_should_not_change_size_and_position() {
        val testFile = createTestFile(size = 100)

        createTestFileSystem().use { fs ->
            val fd = fs.execute(Open, crestOpenFileCommand(testFile))
                .getOrElse { fail("Can not open test file") }

            fs.execute(SeekFd, SeekFd(fd, 50, SET)).onLeft { fail("Can not set new position") }

            fs.execute(FallocateFd, FallocateFd(fd, 10, 80)).onLeft { fail("Can not fallocate") }

            val position = fs.execute(SeekFd, SeekFd(fd, 0, CUR))
                .getOrElse { fail("Can not get current position") }

            assertThat(position).isEqualTo(50)
        }

        assertThat(testFile).fileSize().isEqualTo(100)

        val oldContent: ByteArray = SystemFileSystem.source(testFile).buffered().use {
            it.readByteArray(100)
        }

        assertThat(oldContent.all { it == 0xdd.toByte() }).isTrue()
    }

    @Test
    public fun fallocate_outsize_file_should_change_size() {
        val testFile = createTestFile(size = 100)

        createTestFileSystem().use { fs ->
            val fd = fs.execute(Open, crestOpenFileCommand(testFile))
                .getOrElse { fail("Can not open test file") }
            fs.execute(FallocateFd, FallocateFd(fd, 199, 1)).onLeft { fail("Can not fallocate") }
        }

        assertThat(testFile).fileSize().isEqualTo(200)
    }

    private fun createTestFile(
        name: String = "testFile",
        size: Int = 100,
    ): Path {
        val root: Path = tempFolder.absolutePath()
        val testFile = Path(root, name)

        SystemFileSystem.sink(testFile).buffered().use { sink ->
            sink.write(ByteArray(size) { 0xdd.toByte() })
        }
        return testFile
    }

    private fun crestOpenFileCommand(testfilePath: Path) = Open(
        path = testfilePath.name,
        baseDirectory = BaseDirectory.DirectoryFd(WASI_FIRST_PREOPEN_FD),
        openFlags = OpenFileFlag.O_RDWR,
        fdFlags = 0,
    )
}
