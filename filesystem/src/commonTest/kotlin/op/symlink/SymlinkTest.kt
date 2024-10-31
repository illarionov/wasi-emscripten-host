/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.symlink

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.test.ignore.annotations.IgnoreApple
import at.released.weh.test.ignore.annotations.IgnoreMingw
import at.released.weh.test.utils.absolutePath
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.test.Test

@IgnoreApple
@IgnoreMingw
class SymlinkTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun symlink_success_case() {
        val root: Path = tempFolder.absolutePath()
        val tempfolderFd = WASI_FIRST_PREOPEN_FD

        SystemFileSystem.run {
            sink(Path(root, "testfile.txt")).buffered().use {
                it.writeString("Test content")
            }
        }

        createTestFileSystem().use { fileSystem ->
            fileSystem.execute(
                Symlink,
                Symlink("testfile.txt", "newfile.txt", newPathBaseDirectory = DirectoryFd(tempfolderFd)),
            ).onLeft { error("Can not create symlink: $it") }
        }

        val newFileContent = SystemFileSystem.source(Path(root, "newfile.txt")).buffered().use {
            it.readString()
        }

        assertThat(newFileContent).isEqualTo("Test content")
    }
}
