/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.extracting
import assertk.assertions.isTrue
import at.released.weh.filesystem.model.Filetype
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

class NioDirEntrySequenceTest {
    @JvmField
    @Rule
    var folder: TemporaryFolder = TemporaryFolder()

    @Test
    fun dir_entry_sequence_success_case() {
        val rootPath = folder.root.toPath()
        rootPath.resolve("dir1").createDirectory()
        rootPath.resolve("dir2").createDirectory()
        rootPath.resolve("file1").createFile()

        val directoryStream = Files.newDirectoryStream(rootPath)
        NioDirEntrySequence(rootPath, directoryStream, 1L).use { dirSequence: NioDirEntrySequence ->
            val files = dirSequence.asSequence().toList()
            assertThat(files)
                .extracting { it.name to it.type }
                .containsExactlyInAnyOrder(
                    ".." to Filetype.DIRECTORY,
                    "dir1" to Filetype.DIRECTORY,
                    "file1" to Filetype.REGULAR_FILE,
                    "dir2" to Filetype.DIRECTORY,
                )
        }
    }

    @Test
    fun dir_entry_sequence_should_close_underlying_descriptor() {
        val rootPath = folder.root.toPath()
        val innerDirectoryStream = Files.newDirectoryStream(rootPath)
        val directoryStream = object : DirectoryStream<Path> by innerDirectoryStream {
            var isClosed: Boolean = false
            override fun close() {
                isClosed = true
                innerDirectoryStream.close()
            }
        }
        val sequence = NioDirEntrySequence(rootPath, directoryStream, 0)
        sequence.use {
            it.toList()
        }
        assertThat(directoryStream.isClosed).isTrue()
    }
}
