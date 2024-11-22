/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.truncate

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import assertk.tableOf
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Whence.CUR
import at.released.weh.filesystem.model.Whence.SET
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.seek.SeekFd
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.test.filesystem.assertions.fileSize
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.test.Test
import kotlin.test.fail

class TruncateTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun truncate_shrink_success_case() {
        tableOf("testFileName", "cursorPosition", "expectedNewCursorPosition")
            .row("t1", 10L, 10L)
            .row("t2", 50L, 50L)
            .row("t3", 70L, 50L)
            .forAll { testFileName, cursorPosition, expectedNewPosition ->
                val testFile = tempFolder.createTestFile(testfilePath = testFileName, size = 100)
                createTestFileSystem().use { fs ->
                    val fd = fs.execute(Open, generateOpenFileCommand(testFile))
                        .getOrElse { fail("Can not open test file") }

                    fs.execute(SeekFd, SeekFd(fd, cursorPosition, SET))
                        .onLeft { fail("Can not set new position") }

                    fs.execute(TruncateFd, TruncateFd(fd, 50))
                        .onLeft { fail("Can not truncate") }

                    val position = fs.execute(SeekFd, SeekFd(fd, 0, CUR))
                        .getOrElse { fail("Can not get current position") }

                    assertThat(position).isEqualTo(expectedNewPosition)
                }

                assertThat(testFile).fileSize().isEqualTo(50)
            }
    }

    @Test
    public fun truncate_extend_success_case() {
        tableOf("testFileName", "cursorPosition", "expectedNewCursorPosition")
            .row("t1", 10L, 10L)
            .row("t2", 10L, 10L)
            .row("t3", 150L, 150L)
            .forAll { testFileName, cursorPosition, expectedNewPosition ->
                val testFile = tempFolder.createTestFile(testfilePath = testFileName, size = 100)
                createTestFileSystem().use { fs ->
                    val fd = fs.execute(Open, generateOpenFileCommand(testFile))
                        .getOrElse { fail("Can not open test file") }

                    fs.execute(SeekFd, SeekFd(fd, cursorPosition, SET))
                        .onLeft { fail("Can not set new position") }

                    fs.execute(TruncateFd, TruncateFd(fd, 200))
                        .onLeft { fail("Can not truncate") }

                    val position = fs.execute(SeekFd, SeekFd(fd, 0, CUR))
                        .getOrElse { fail("Can not get current position") }

                    assertThat(position).isEqualTo(expectedNewPosition)
                }

                assertThat(testFile).fileSize().isEqualTo(200)

                val (oldContent, newContent) = SystemFileSystem.source(testFile).buffered().use {
                    val old = it.readByteArray(100)
                    val new = it.readByteArray(100)
                    old to new
                }

                assertThat(oldContent.all { it == 0xdd.toByte() }).isTrue()
                assertThat(newContent.all { it == 0.toByte() }).isTrue()
            }
    }

    private fun generateOpenFileCommand(testfilePath: Path) = Open(
        path = testfilePath.name,
        baseDirectory = BaseDirectory.DirectoryFd(WASI_FIRST_PREOPEN_FD),
        openFlags = OpenFileFlag.O_RDWR,
        fdFlags = 0,
    )
}
