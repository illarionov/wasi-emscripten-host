/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.tableOf
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_CURRENT_DIR_ENTRY
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_PARENT_DIR_ENTRY
import kotlin.test.Test

class OffsetCookieDecoratorTest {
    @Test
    fun offset_cookie_should_add_cookies() {
        val testDirEntry = DirEntry("testdir", DIRECTORY, 0, 0)

        val iterator = OffsetCookieDecorator(
            delegate = listOf(
                TEST_CURRENT_DIR_ENTRY,
                TEST_PARENT_DIR_ENTRY,
                testDirEntry,
            ).iterator(),
        )

        val files: List<DirEntry> = iterator.asSequence().toList()

        assertThat(files).containsExactlyInAnyOrder(
            TEST_CURRENT_DIR_ENTRY.copy(cookie = 0),
            TEST_PARENT_DIR_ENTRY.copy(cookie = 1),
            testDirEntry.copy(cookie = 2),
        )
    }

    @Test
    fun offset_cookie_test_empty_list() {
        val iterator = OffsetCookieDecorator(delegate = listOf<DirEntry>().iterator())

        val files: List<DirEntry> = iterator.asSequence().toList()

        assertThat(files).isEmpty()
    }

    @Test
    fun start_offset_should_work() {
        val dir0 = TEST_CURRENT_DIR_ENTRY
        val dir1 = TEST_PARENT_DIR_ENTRY
        val dir2 = DirEntry("testdir", DIRECTORY, 0, 0)

        tableOf("list", "startOffset", "expectedResult")
            .row(
                listOf(dir0, dir1, dir2),
                1L,
                listOf(dir1.copy(cookie = 1), dir2.copy(cookie = 2)),
            )
            .row(
                listOf(dir0, dir1, dir2),
                2L,
                listOf(dir2.copy(cookie = 2)),
            )
            .row(
                listOf(dir0, dir1, dir2),
                3L,
                listOf(),
            )
            .row(
                listOf(dir0, dir1, dir2),
                4L,
                listOf(),
            )
            .row(listOf(), 0L, listOf())
            .row(listOf(), 1L, listOf())
            .forAll { sourceDirectories, startOffset, expectedDirEntries ->
                val iterator = OffsetCookieDecorator(
                    delegate = sourceDirectories.iterator(),
                    startOffset = startOffset,
                )

                val files: List<DirEntry> = iterator.asSequence().toList()

                assertThat(files).containsExactlyInAnyOrder(*expectedDirEntries.toTypedArray())
            }
    }
}
