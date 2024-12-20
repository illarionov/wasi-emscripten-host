/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readlink

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_FIRST_PREOPEN_FD
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.filesystem.testutil.BaseFileSystemIntegrationTest
import at.released.weh.filesystem.testutil.createSymlink
import at.released.weh.test.utils.absolutePath
import kotlinx.io.files.Path
import kotlin.test.Test

class ReadLinkTest : BaseFileSystemIntegrationTest() {
    @Test
    public fun readlink_success_case() {
        val root: Path = tempFolder.absolutePath()
        val tempfolderFd = WASI_FIRST_PREOPEN_FD

        createSymlink("../target", Path(root, "testlink"))
        createTestFileSystem().use { fileSystem ->
            val symlinkTarget: VirtualPath = fileSystem.execute(
                ReadLink,
                ReadLink(
                    path = "testlink".toVirtualPath(),
                    baseDirectory = DirectoryFd(tempfolderFd),
                ),
            ).getOrElse {
                error("Read symlink error: $it")
            }

            assertThat(symlinkTarget.toString()).isEqualTo("../target")
        }
    }
}
