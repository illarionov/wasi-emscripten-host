/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.setfdflags

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isIn
import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.op.fdattributes.FdAttributes
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_RDONLY
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createTestFile
import at.released.weh.filesystem.testutil.op.createForTestFile
import kotlin.test.Test
import kotlin.test.fail

class SetFdflagsTest : BaseFileSystemIntegrationTest() {
    @Test
    fun set_fdflags_success_case() {
        val testFile = tempFolder.createTestFile()
        val fdFlags = FdFlag.FD_APPEND or FdFlag.FD_SYNC
        createTestFileSystem().use { fs ->
            val openRequest = Open.createForTestFile(testFile, O_RDONLY, fdFlags)
            val fd = fs.execute(Open, openRequest).getOrElse { fail("Open error: $it") }
            fs.execute(SetFdFlags, SetFdFlags(fd, fdFlags)).getOrElse { fail("SetFdFlags failed: $it") }
        }
    }

    @Test
    fun set_fdflags_reset_flags() {
        val testFile = tempFolder.createTestFile()
        createTestFileSystem().use { fs ->
            val openRequest = Open.createForTestFile(testFile, O_RDONLY, FdFlag.FD_APPEND or FdFlag.FD_SYNC)
            val fd = fs.execute(Open, openRequest).getOrElse { fail("Open error: $it") }

            fs.execute(SetFdFlags, SetFdFlags(fd, 0)).getOrElse { fail("SetFdFlags failed: $it") }

            val newFlags = fs.execute(FdAttributes, FdAttributes(fd)).map { it.flags }.getOrElse {
                fail("FdAttributes failed: $it")
            }
            assertThat(newFlags)
                .isIn(
                    0, // Apple
                    FdFlag.FD_SYNC, // JVM, Windows
                    FdFlag.FD_DSYNC or FdFlag.FD_RSYNC or FdFlag.FD_SYNC, // Linux, sync flag is not changeable
                )
        }
    }
}
