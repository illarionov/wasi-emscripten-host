/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.readdir

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.extracting
import assertk.assertions.isEmpty
import assertk.assertions.isInstanceOf
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.readdir.DirEntry
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.Test

class NioDirectoryIteratorTest {
    @JvmField
    @Rule
    var folder: TemporaryFolder = TemporaryFolder()

    @Test
    fun directory_iterator_success_case() {
        val rootPath = folder.root.toPath()
        val rootDir1: Path = rootPath.resolve("dir1").createDirectory()
        rootDir1.resolve("dir12").createDirectory()
        rootPath.resolve("dir2").createDirectory()
        rootPath.resolve("file1").createFile()

        Files.newDirectoryStream(folder.root.toPath()).use { directoryStream ->
            val iterator: Iterator<DirEntry> = NioDirectoryIterator(
                folder.root.toPath(),
                directoryStream.iterator(),
            )

            val files = iterator.asSequence().toList()
            assertThat(files)
                .extracting { it.name to it.type }
                .containsExactlyInAnyOrder(
                    "dir1" to Filetype.DIRECTORY,
                    "dir2" to Filetype.DIRECTORY,
                    "file1" to Filetype.REGULAR_FILE,
                )
        }
    }

    @Test
    fun directory_iterator_check_empty_directory() {
        Files.newDirectoryStream(folder.root.toPath()).use { directoryStream ->
            val iterator: Iterator<DirEntry> = NioDirectoryIterator(folder.root.toPath(), directoryStream.iterator())

            val files = iterator.asSequence().toList()
            assertThat(files).isEmpty()
        }
    }

    @Test
    fun directory_iterator_should_throw_no_such_field_exception_on_exhausted() {
        Files.newDirectoryStream(folder.root.toPath()).use { directoryStream ->
            val iterator: Iterator<DirEntry> = NioDirectoryIterator(folder.root.toPath(), directoryStream.iterator())

            assertFailure {
                iterator.next()
            }.isInstanceOf(NoSuchElementException::class)
        }
    }
}
