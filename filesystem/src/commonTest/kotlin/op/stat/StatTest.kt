/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import arrow.core.getOrElse
import assertk.assertThat
import at.released.weh.filesystem.op.stat.StatFdTest.Companion.directoryAttributesLooksCorrect
import at.released.weh.filesystem.op.stat.StatFdTest.Companion.fileAttributesLooksCorrect
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import at.released.weh.test.ignore.annotations.IgnoreApple
import at.released.weh.test.ignore.annotations.IgnoreJvm
import at.released.weh.test.ignore.annotations.IgnoreLinux
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.fail

class StatTest : BaseFileSystemIntegrationTest() {
    @Test
    @IgnoreLinux // TODO: check
    @IgnoreJvm
    @IgnoreApple
    fun statfd_file_success_case() {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val testFile = tempFolder.createTestFile(size = 100)
        createTestFileSystem().use { fs ->
            val statRequest = Stat(testFile.name, tempFolderDirectoryFd, true)
            val stat: StructStat = fs.execute(Stat, statRequest).getOrElse { fail("Stat() failed: $it") }
            assertThat(stat).fileAttributesLooksCorrect(currentTimeMillis)
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
            val statRequest = Stat(tempDirectory.name, tempFolderDirectoryFd, true)
            val stat: StructStat = fs.execute(Stat, statRequest).getOrElse { fail("Stat() failed: $it") }
            assertThat(stat).directoryAttributesLooksCorrect(currentTimeMillis)
        }
    }
}
