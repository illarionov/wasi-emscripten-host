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
            assertThat(statfd).fileAttributesLooksCorrect(currentTimeMillis)
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
            assertThat(statfd).directoryAttributesLooksCorrect(currentTimeMillis)
        }
    }

    internal companion object {
        private const val MAX_TIME_GAP_MS = 10000L
        internal fun Assert<StructStat>.fileAttributesLooksCorrect(
            startTimeMs: Long,
        ) = all {
            prop(StructStat::deviceId).isNotZero() // can be null, but usually different
            prop(StructStat::inode).isNotZero() // if inodes are not available, we substitute fake values
            prop(StructStat::type).isEqualTo(REGULAR_FILE)
            prop(StructStat::links).isGreaterThanOrEqualTo(1)
            prop(StructStat::size).isEqualTo(100)
            prop(StructStat::blockSize).isGreaterThan(0)
            prop(StructStat::blocks).isEqualTo(1) // blocks are usually larger than 100 bytes
            prop(StructStat::accessTime).isBetweenMillis(startTimeMs, startTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::modificationTime).isBetweenMillis(startTimeMs, startTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::changeStatusTime).isBetweenMillis(startTimeMs, startTimeMs + MAX_TIME_GAP_MS)
        }

        internal fun Assert<StructStat>.directoryAttributesLooksCorrect(
            startTimeMs: Long,
        ) = all {
            prop(StructStat::deviceId).isNotZero() // can be null, but usually different
            prop(StructStat::inode).isNotZero() // if inodes are not available, we substitute fake values
            prop(StructStat::type).isEqualTo(DIRECTORY)
            prop(StructStat::links).isGreaterThanOrEqualTo(1)
            prop(StructStat::size).isEqualTo(0)
            prop(StructStat::blockSize).isGreaterThan(0)
            prop(StructStat::accessTime).isBetweenMillis(startTimeMs, startTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::modificationTime).isBetweenMillis(startTimeMs, startTimeMs + MAX_TIME_GAP_MS)
            prop(StructStat::changeStatusTime).isBetweenMillis(startTimeMs, startTimeMs + MAX_TIME_GAP_MS)
        }

        internal fun Assert<StructTimespec>.isBetweenMillis(start: Long, end: Long) = prop(StructTimespec::timeMillis)
            .isBetween(start, end)
    }
}
