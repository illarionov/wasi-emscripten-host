/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import arrow.core.getOrElse
import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isNotZero
import assertk.assertions.prop
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_RDONLY
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.op.createForTestDirectory
import at.released.weh.filesystem.testutil.op.createForTestFile
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.Clock

class StatFdTest : BaseFileSystemIntegrationTest() {
    @Test
    fun statfd_file_success_case() {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val testFile = tempFolder.createTestFile(size = 100)
        createTestFileSystem().use { fs ->
            val openRequest = Open.createForTestFile(testFile, O_RDONLY)
            val fd = fs.execute(Open, openRequest).getOrElse { fail("Open error: $it") }
            val statfd = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Statfd() failed: $it") }
            assertThat(statfd).fileAttributesLooksCorrect(currentTimeMillis)
        }
    }

    @Test
    fun statfd_directory_success_case() {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val tempDirectory = tempFolder.createTestDirectory()
        createTestFileSystem().use { fs ->
            val openRequest = Open.createForTestDirectory(tempDirectory)
            val fd = fs.execute(Open, openRequest).getOrElse { fail("Open error: $it") }
            val statfd = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Statfd() failed: $it") }
            assertThat(statfd).directoryAttributesLooksCorrect(currentTimeMillis)
        }
    }

    internal companion object {
        private const val MAX_TIME_GAP_MS = 10000L
        internal fun Assert<StructStat>.fileAttributesLooksCorrect(
            startTimeMs: Long,
        ) = all {
            // deviceId can be null, but usually different
            prop(StructStat::deviceId).isNotZero() // can be null, but usually different

            // if inodes are not available, we substitute fake values
            prop(StructStat::inode).isNotZero()
            prop(StructStat::type).isEqualTo(REGULAR_FILE)
            prop(StructStat::links).isGreaterThanOrEqualTo(1)
            prop(StructStat::size).isEqualTo(100)
            prop(StructStat::blockSize).isGreaterThan(0)

            // Blocks are usually larger than 100 bytes, so we consider the minimum to be 1.
            // The value of 8 blocks (4096 bytes) was found on Linux and may vary.
            prop(StructStat::blocks).isBetween(1, 16)

            // On Linux, for reasons, the timestamp on the file may be a few milliseconds earlier
            // than the timestamp returned just before the file was created
            val minTimeMs = startTimeMs - 100
            prop(StructStat::accessTime).isBetweenMillis(minTimeMs, minTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::modificationTime).isBetweenMillis(minTimeMs, minTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::changeStatusTime).isBetweenMillis(minTimeMs, minTimeMs + MAX_TIME_GAP_MS)
        }

        internal fun Assert<StructStat>.directoryAttributesLooksCorrect(
            startTimeMs: Long,
        ) = all {
            // deviceId can be null, but usually different
            prop(StructStat::deviceId).isNotZero()

            // if inodes are not available on real file system, we substitute fake values
            prop(StructStat::inode).isNotZero()
            prop(StructStat::type).isEqualTo(DIRECTORY)
            prop(StructStat::links).isGreaterThanOrEqualTo(1)

            // Size of the directory entry is not always equal to 0 on Linux
            prop(StructStat::size).isBetween(0, 131072)
            prop(StructStat::blockSize).isGreaterThan(0)

            // On Linux, for reasons, the timestamp on the file may be a few milliseconds earlier
            // than the timestamp returned just before the file was created
            val minTimeMs = startTimeMs - 100
            prop(StructStat::accessTime).isBetweenMillis(minTimeMs, startTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::modificationTime).isBetweenMillis(minTimeMs, startTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::changeStatusTime).isBetweenMillis(minTimeMs, startTimeMs + MAX_TIME_GAP_MS)
        }

        internal fun Assert<StructTimespec>.isBetweenMillis(start: Long, end: Long) = prop(StructTimespec::timeMillis)
            .isBetween(start, end)
    }
}
