/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.readdir

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isInstanceOf
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.posix.readdir.PosixReadDirResult.EndOfStream
import at.released.weh.filesystem.posix.readdir.PosixReadDirResult.Entry
import at.released.weh.filesystem.posix.readdir.PosixReadDirResult.Error
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_CURRENT_DIR_ENTRY
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_PARENT_DIR_ENTRY
import kotlinx.io.IOException
import kotlin.test.Test

class PosixDirectoryIteratorTest {
    @Test
    fun linuxDirectoryIterator_success_case() {
        @Suppress("MagicNumber")
        val testDirEntry = DirEntry("testFile", REGULAR_FILE, 42, 44)
        var nextDirCounter = 0
        val iterator = PosixDirectoryIterator(
            next = Entry(TEST_CURRENT_DIR_ENTRY),
            streamIsClosed = { false },
            nextDirProvider = {
                when (nextDirCounter++) {
                    0 -> Entry(TEST_PARENT_DIR_ENTRY)
                    1 -> Entry(testDirEntry)
                    2 -> EndOfStream
                    else -> error("Should not be called")
                }
            },
        )

        val directories = iterator.asSequence().toList()

        assertThat(directories).containsExactly(TEST_CURRENT_DIR_ENTRY, TEST_PARENT_DIR_ENTRY, testDirEntry)
    }

    @Test
    fun posixDirectoryIterator_empty_list() {
        val iterator = PosixDirectoryIterator(
            next = EndOfStream,
            streamIsClosed = { false },
            nextDirProvider = { error("Should not be called") },
        )

        val directories = iterator.asSequence().toList()

        assertThat(directories).isEmpty()
    }

    @Test
    fun posixDirectoryIterator_test_error() {
        val iterator = PosixDirectoryIterator(
            next = Error(BadFileDescriptor("test error")),
            streamIsClosed = { false },
            nextDirProvider = { error("Should not be called") },
        )

        assertFailure {
            iterator.asSequence().toList()
        }.isInstanceOf(IOException::class)
    }
}
