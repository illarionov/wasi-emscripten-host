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
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isNotZero
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_RDONLY
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.op.createForTestDirectory
import at.released.weh.filesystem.testutil.op.createForTestFile
import at.released.weh.test.ignore.annotations.IgnoreApple
import at.released.weh.test.ignore.annotations.IgnoreJvm
import at.released.weh.test.ignore.annotations.IgnoreLinux
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.fail

class StatFdTest : BaseFileSystemIntegrationTest() {
    @Test
    @IgnoreLinux // TODO: check
    @IgnoreJvm
    @IgnoreApple
    fun statfd_file_success_case() {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val testFile = tempFolder.createTestFile(size = 100)

        createTestFileSystem().use { fs ->
            val openRequest = Open.createForTestFile(testFile, O_RDONLY)

            val fd = fs.execute(Open, openRequest).getOrElse { fail("Open error: $it") }

            val statfd = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Statfd() failed: $it") }

            assertThat(statfd.deviceId).isNotZero() // can be null, but usually different
            assertThat(statfd.inode).isNotZero() // if inodes are not available, we substitute fake values
            assertThat(statfd.type).isEqualTo(REGULAR_FILE)
            assertThat(statfd.links).isGreaterThanOrEqualTo(1)
            assertThat(statfd.size).isEqualTo(100)
            assertThat(statfd.blockSize).isGreaterThan(0)
            assertThat(statfd.blocks).isEqualTo(1) // blocks are usually larger than 100 bytes
            assertThat(statfd.accessTime.timeMillis).isBetween(currentTimeMillis, currentTimeMillis + MAX_TIME_GAP_MS)
            assertThat(statfd.modificationTime.timeMillis).isBetween(
                currentTimeMillis,
                currentTimeMillis + MAX_TIME_GAP_MS,
            )
            assertThat(statfd.changeStatusTime.timeMillis).isBetween(
                currentTimeMillis,
                currentTimeMillis + MAX_TIME_GAP_MS,
            )
        }
    }

    @Test
    @IgnoreLinux // TODO: check
    @IgnoreJvm
    @IgnoreApple
    fun statfd_directory_success_case() {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()

        val tempDirectory = tempFolder.createTestDirectory()

        createTestFileSystem().use { fs ->
            val openRequest = Open.createForTestDirectory(tempDirectory)

            val fd = fs.execute(Open, openRequest).getOrElse { fail("Open error: $it") }

            val statfd = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Statfd() failed: $it") }

            assertThat(statfd.deviceId).isNotZero() // can be null, but usually different
            assertThat(statfd.inode).isNotZero() // if inodes are not available, we substitute fake values
            assertThat(statfd.type).isEqualTo(DIRECTORY)
            assertThat(statfd.links).isGreaterThanOrEqualTo(1)
            assertThat(statfd.size).isEqualTo(0)
            assertThat(statfd.blockSize).isGreaterThan(0)
            assertThat(statfd.accessTime.timeMillis).isBetween(currentTimeMillis, currentTimeMillis + MAX_TIME_GAP_MS)
            assertThat(statfd.modificationTime.timeMillis).isBetween(
                currentTimeMillis,
                currentTimeMillis + MAX_TIME_GAP_MS,
            )
            assertThat(statfd.changeStatusTime.timeMillis).isBetween(
                currentTimeMillis,
                currentTimeMillis + MAX_TIME_GAP_MS,
            )
        }
    }

    private companion object {
        private const val MAX_TIME_GAP_MS = 10000L
    }
}
