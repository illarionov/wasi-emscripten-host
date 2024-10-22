/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.readdir.DirEntry
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.stdio.JvmStandardInputOutput
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.test.fail

class NioReadDirFdTest {
    @JvmField
    @Rule
    public var folder: TemporaryFolder = TemporaryFolder()
    internal var fileSystemState: NioFileSystemState? = null

    @Before
    fun setup() {
        val rootDir1: Path = folder.newFolder("dir1").toPath()
        rootDir1.resolve("dir12").createDirectory()
        folder.newFolder("dir2")
        folder.newFile("file1")
    }

    @After
    fun cleanup() {
        fileSystemState?.close()
    }

    @Test
    fun readdir_success_case() {
        val tempfolderFd = WASI_FIRST_PREOPEN_FD
        fileSystemState = NioFileSystemState.create(
            stdio = JvmStandardInputOutput,
            isRootAccessAllowed = false,
            currentWorkingDirectory = folder.root.path,
            preopenedDirectories = emptyList(),
        )
        val readdirFd = NioReadDirFd(fileSystemState!!)

        val files: List<DirEntry> = readdirFd.invoke(ReadDirFd(tempfolderFd)).getOrElse {
            fail("error: $it")
        }.use { sequence ->
            sequence.toList()
        }

        assertThat(files.map { it.name to it.type })
            .containsExactlyInAnyOrder(
                "." to Filetype.DIRECTORY,
                ".." to Filetype.DIRECTORY,
                "dir1" to Filetype.DIRECTORY,
                "dir2" to Filetype.DIRECTORY,
                "file1" to Filetype.REGULAR_FILE,
            )
    }
}
