/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readdir

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.test.utils.absolutePath
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test

class ReadDirTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun read_dir_should_work() {
        val root: Path = tempFolder.absolutePath()
        val tempfolderFd = WASI_FIRST_PREOPEN_FD

        SystemFileSystem.run {
            createDirectories(Path(root, "dir1"))
            createDirectories(Path(root, "dir1/dir12"))
            createDirectories(Path(root, "dir2"))
            sink(Path(root, "file1")).close()
        }

        val request = ReadDirFd(tempfolderFd)

        createTestFileSystem().use { fileSystem ->
            val closableSequence = fileSystem.execute(ReadDirFd, request).getOrElse {
                error("Read dir error: $it")
            }
            val directories = closableSequence.use(DirEntrySequence::toList)
            assertThat(directories.map { it.name to it.type })
                .containsExactlyInAnyOrder(
                    "." to Filetype.DIRECTORY,
                    ".." to Filetype.DIRECTORY,
                    "dir1" to Filetype.DIRECTORY,
                    "dir2" to Filetype.DIRECTORY,
                    "file1" to Filetype.REGULAR_FILE,
                )
        }
    }
}
