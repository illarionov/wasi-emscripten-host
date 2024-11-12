/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readwrite

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.test.ignore.annotations.IgnoreMingw
import kotlin.test.Test

@IgnoreMingw
public class WriteFdChangePositionTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun write_with_emty_iovecs_should_check_fd() {
        val request = WriteFd(
            fd = -327542,
            cIovecs = emptyList(),
            strategy = ReadWriteStrategy.CurrentPosition,
        )

        val errNo = createTestFileSystem().use { fileSystem ->
            fileSystem.execute(WriteFd, request).fold(
                ifLeft = { it.errno },
                ifRight = { FileSystemErrno.SUCCESS },
            )
        }

        assertThat(errNo).isEqualTo(FileSystemErrno.BADF)
    }
}
