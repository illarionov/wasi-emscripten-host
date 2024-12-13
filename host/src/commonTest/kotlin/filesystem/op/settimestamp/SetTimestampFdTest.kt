/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.settimestamp

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.stat.timeNanos
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.op.createForTestDirectory
import at.released.weh.filesystem.testutil.op.createForTestFile
import kotlin.test.Test
import kotlin.test.fail

class SetTimestampFdTest : BaseFileSystemIntegrationTest() {
    @Test
    fun set_timestampfd_change_atime_mtime_on_file_success_case() {
        tableOf("fileName", "testAtime", "testMtime")
            .row<String, Long?, Long?>("t1", TEST_ATIME_NS, TEST_MTIME_NS)
            .row("t2", null, TEST_MTIME_NS)
            .row("t3", TEST_ATIME_NS, null)
            .row("t4", null, null)
            .forAll { fileName, testAtime, testMtime ->
                val testFile = tempFolder.createTestFile(fileName)
                val (oldStat, newStat) = createTestFileSystem().use { fs ->
                    val fd = fs.execute(Open, Open.createForTestFile(testFile))
                        .getOrElse { fail("Can not open test file") }

                    val oldStat = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Can not get stat") }

                    fs.execute(SetTimestampFd, SetTimestampFd(fd, testAtime, testMtime))
                        .onLeft { fail("Can not set timestamp") }

                    val newStat = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Can not get stat") }

                    oldStat to newStat
                }

                if (testAtime != null) {
                    assertThat(newStat.accessTime.timeNanos).isEqualTo(testAtime)
                } else {
                    assertThat(newStat.accessTime).isEqualTo(oldStat.accessTime)
                }
                if (testMtime != null) {
                    assertThat(newStat.modificationTime.timeNanos).isEqualTo(testMtime)
                } else {
                    assertThat(newStat.modificationTime).isEqualTo(oldStat.modificationTime)
                }
            }
    }

    @Test
    fun set_timestampfd_change_atime_mtime_on_directory_success_case() {
        tableOf("directoryName", "testAtime", "testMtime")
            .row<String, Long?, Long?>("d1", TEST_ATIME_NS, TEST_MTIME_NS)
            .row("d2", null, TEST_MTIME_NS)
            .row("d3", TEST_ATIME_NS, null)
            .row("d4", null, null)
            .forAll { fileName, testAtime, testMtime ->
                val testDirectory = tempFolder.createTestDirectory(fileName)
                val (oldStat, newStat) = createTestFileSystem().use { fs ->
                    val fd = fs.execute(Open, Open.createForTestDirectory(testDirectory))
                        .getOrElse { fail("Can not open test directory") }

                    val oldStat = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Can not get stat") }

                    fs.execute(SetTimestampFd, SetTimestampFd(fd, testAtime, testMtime))
                        .onLeft { fail("Can not set timestamp") }

                    val newStat = fs.execute(StatFd, StatFd(fd)).getOrElse { fail("Can not get stat") }

                    oldStat to newStat
                }

                if (testAtime != null) {
                    assertThat(newStat.accessTime.timeNanos).isEqualTo(testAtime)
                } else {
                    assertThat(newStat.accessTime).isEqualTo(oldStat.accessTime)
                }
                if (testMtime != null) {
                    assertThat(newStat.modificationTime.timeNanos).isEqualTo(testMtime)
                } else {
                    assertThat(newStat.modificationTime).isEqualTo(oldStat.modificationTime)
                }
            }
    }

    private companion object {
        private const val TEST_ATIME_NS = 1_732_361_361_345_520_000L
        private const val TEST_MTIME_NS = 1_762_361_315_345_510_000L
    }
}
