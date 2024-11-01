/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_CURRENT_DIR_ENTRY
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_PARENT_DIR_ENTRY
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test

class SpecialDirectoryEntriesDecoratorTest {
    @Test
    fun special_directory_success_case() {
        val dirEntryReader: (name: String, realPath: Path, cookie: Long) -> DirEntry = { name, _, _ ->
            when (name) {
                "." -> TEST_CURRENT_DIR_ENTRY
                ".." -> TEST_PARENT_DIR_ENTRY
                else -> error("Not expected to be called")
            }
        }

        val testDirEntry = DirEntry("testdir", DIRECTORY, 0, 0)

        val iterator = SpecialDirectoryEntriesDecorator(
            rootPath = Path("/"),
            delegate = listOf(testDirEntry).iterator(),
            streamIsClosed = { false },
            dirEntryReader = dirEntryReader,
        )

        val files: List<DirEntry> = iterator.asSequence().toList()

        assertThat(files).containsExactlyInAnyOrder(TEST_CURRENT_DIR_ENTRY, TEST_PARENT_DIR_ENTRY, testDirEntry)
    }
}
