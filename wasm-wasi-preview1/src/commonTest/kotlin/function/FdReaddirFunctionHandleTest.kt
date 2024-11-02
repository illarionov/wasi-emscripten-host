/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_CURRENT_DIR_ENTRY
import at.released.weh.filesystem.test.fixtures.readdir.TestDirEntry.TEST_PARENT_DIR_ENTRY
import at.released.weh.wasi.preview1.ext.DIRENT_PACKED_SIZE
import at.released.weh.wasi.preview1.function.FdReaddirFunctionHandle.Companion.packDirEntriesToBuf
import kotlinx.io.Buffer
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.readByteString
import kotlinx.io.readIntLe
import kotlinx.io.readLongLe
import kotlin.test.Test

public class FdReaddirFunctionHandleTest {
    @Test
    fun packDirEntriesToBuf_should_pack_entries() {
        val testDirEntry = DirEntry(
            name = "testEntry",
            type = REGULAR_FILE,
            inode = 10,
            cookie = 11,
        )
        val dirEntries = sequenceOf(TEST_CURRENT_DIR_ENTRY, TEST_PARENT_DIR_ENTRY, testDirEntry)
        val buffer = Buffer()
        val maxSize = 1024

        val bytesWritten = packDirEntriesToBuf(dirEntries, buffer, maxSize).getOrElse { "Pack() failed" }

        val expectedSize = dirEntries.sumOf { DIRENT_PACKED_SIZE + it.name.length }
        assertThat(bytesWritten).isEqualTo(expectedSize)

        buffer.run {
            dirEntries.forEach { expectedDirEntry -> validateDirEntry(expectedDirEntry) }
        }
    }

    @Test
    fun packDirEntriesToBuf_test_exact_max_size() {
        val dirEntries = sequenceOf(TEST_CURRENT_DIR_ENTRY, TEST_PARENT_DIR_ENTRY)
        val buffer = Buffer()
        val maxSize = dirEntries.sumOf { DIRENT_PACKED_SIZE + it.name.length }

        val bytesWritten = packDirEntriesToBuf(dirEntries, buffer, maxSize).getOrElse { "Pack() failed" }

        assertThat(bytesWritten).isEqualTo(maxSize)
        buffer.run {
            dirEntries.forEach { expectedDirEntry -> validateDirEntry(expectedDirEntry) }
        }
    }

    @Test
    fun packDirEntriesToBuf_test_truncation() {
        val dirEntries = sequenceOf(TEST_CURRENT_DIR_ENTRY, TEST_PARENT_DIR_ENTRY)
        val testPack = Buffer().let { buffer ->
            val maxSize = 1024
            packDirEntriesToBuf(dirEntries, buffer, maxSize).getOrElse { "Pack() failed" }
            buffer.readByteString()
        }

        for (testMaxSize in 0..<testPack.size) {
            val buffer = Buffer()
            val bytesWritten = packDirEntriesToBuf(dirEntries, buffer, testMaxSize).getOrElse { "Pack() failed" }
            assertThat(bytesWritten == testMaxSize)
            assertThat(buffer.readByteString()).isEqualTo(testPack.substring(0, testMaxSize))
        }
    }

    private fun Buffer.validateDirEntry(dirEntry: DirEntry) = run {
        assertThat(readLongLe()).isEqualTo(dirEntry.cookie)
        assertThat(readLongLe()).isEqualTo(dirEntry.inode)
        assertThat(readIntLe()).isEqualTo(dirEntry.name.length)
        assertThat(readIntLe()).isEqualTo(dirEntry.type.id)
        assertThat(readByteString(dirEntry.name.length).decodeToString()).isEqualTo(dirEntry.name)
    }
}
