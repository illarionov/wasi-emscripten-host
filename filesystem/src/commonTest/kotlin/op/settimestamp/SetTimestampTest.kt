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
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.op.settimestamp.SetTimestampTestFixtures.TEST_ATIME_NS
import at.released.weh.filesystem.op.settimestamp.SetTimestampTestFixtures.TEST_MTIME_NS
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.timeNanos
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestDirectory
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.tempFolderDirectoryFd
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.fail

class SetTimestampTest : BaseFileSystemIntegrationTest() {
    @Test
    fun set_timestamp_change_atime_mtime_on_file_or_directory_success_case() {
        tableOf("isDirectory", "fileName", "testAtime", "testMtime")
            .row<Boolean, String, Long?, Long?>(false, "f1", TEST_ATIME_NS, TEST_MTIME_NS)
            .row(false, "f2", null, TEST_MTIME_NS)
            .row(false, "f3", TEST_ATIME_NS, null)
            .row(false, "f4", null, null)
            .row(true, "d1", TEST_ATIME_NS, TEST_MTIME_NS)
            .row(true, "d2", null, TEST_MTIME_NS)
            .row(true, "d3", TEST_ATIME_NS, null)
            .row(true, "d4", null, null)
            .forAll { isDirectory, fileName, testAtime, testMtime ->
                val testFile = if (isDirectory) {
                    tempFolder.createTestDirectory(fileName)
                } else {
                    tempFolder.createTestFile(fileName)
                }

                val (oldStat, newStat) = createTestFileSystem().use { fs ->
                    val oldStat = fs.getFileStat(testFile)

                    val request = SetTimestamp(
                        path = testFile.name.toVirtualPath(),
                        baseDirectory = tempFolderDirectoryFd,
                        atimeNanoseconds = testAtime,
                        mtimeNanoseconds = testMtime,
                        followSymlinks = true,
                    )

                    fs.execute(SetTimestamp, request).onLeft { fail("Can not set timestamp") }

                    val newStat = fs.getFileStat(testFile)
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

    private fun FileSystem.getFileStat(path: Path): StructStat {
        return execute(
            Stat,
            Stat(path.name.toVirtualPath(), tempFolderDirectoryFd),
        ).getOrElse { fail("Can not get stat") }
    }
}
