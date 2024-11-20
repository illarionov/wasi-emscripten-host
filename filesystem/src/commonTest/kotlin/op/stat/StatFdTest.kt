/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotZero
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.test.utils.absolutePath
import kotlinx.datetime.Clock
import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.fail

class StatFdTest : BaseFileSystemIntegrationTest() {
    @Test
    fun statfd_file_success_case() {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()

        setTempfolderContent()

        createTestFileSystem().use { fs ->
            val openRequest = Open(
                path = "file1",
                baseDirectory = BaseDirectory.DirectoryFd(WASI_FIRST_PREOPEN_FD),
                openFlags = OpenFileFlag.O_RDONLY,
                fdFlags = 0,
            )
            val fd = fs.execute(Open, openRequest).getOrElse { fail("Open error: $it") }

            val statfd = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Statfd() failed: $it") }

            assertThat(statfd.deviceId).isNotZero() // can be null, but usually different
            assertThat(statfd.inode).isNotZero() // if inodes are not available, we substitute fake values
            assertThat(statfd.type).isEqualTo(REGULAR_FILE)
            assertThat(statfd.links).isEqualTo(1)
            assertThat(statfd.size).isEqualTo(100)
            assertThat(statfd.blockSize).isGreaterThan(0)
            assertThat(statfd.blocks).isEqualTo(1) // blocks are usually larger than 100 bytes
            assertThat(statfd.accessTime.timeMillis).isBetween(currentTimeMillis, currentTimeMillis + 1000)
            assertThat(statfd.modificationTime.timeMillis).isBetween(currentTimeMillis, currentTimeMillis + 1000)
            assertThat(statfd.changeStatusTime.timeMillis).isBetween(currentTimeMillis, currentTimeMillis + 1000)
        }
    }

    private fun setTempfolderContent() {
        val root: Path = tempFolder.absolutePath()

        val file1Content = Buffer().also { buffer ->
            val bytes = ByteArray(100) { it.toByte() }
            buffer.write(bytes)
        }

        SystemFileSystem.run {
            sink(Path(root, "file1")).use {
                it.write(file1Content, file1Content.size)
            }
        }
    }
}
