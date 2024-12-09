/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.rename.Rename
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.ext.toVirtualPath
import at.released.weh.wasi.preview1.ext.writeFilesystemPath
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PathRenameFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val memory = TestMemory()
    private val host = TestEmbedderHost(
        fileSystem = fileSystem,
    )
    private val pathRenameFunctionHandle = PathRenameFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
    fun path_rename_success_case() {
        var renameRequest: Rename? = null
        fileSystem.onOperation(Rename) { request ->
            renameRequest = request
            Unit.right()
        }

        val oldPath = "target".toVirtualPath()
        val newPath = "newPath".toVirtualPath()

        val oldpathAddr = 0x10
        val oldPathBinarySize = memory.writeFilesystemPath(oldpathAddr, oldPath)

        val newPathAddr = 0x40
        val newPathBinarySize = memory.writeFilesystemPath(newPathAddr, newPath)

        val baseDirectoryFd = 3
        val errNo = pathRenameFunctionHandle.execute(
            memory,
            baseDirectoryFd,
            oldpathAddr,
            oldPathBinarySize,
            baseDirectoryFd,
            newPathAddr,
            newPathBinarySize,
        )

        assertThat(errNo).isEqualTo(SUCCESS)
        assertThat(renameRequest).isEqualTo(
            Rename(
                oldPath = oldPath.toString(),
                newPath = newPath.toString(),
                oldBaseDirectory = BaseDirectory.DirectoryFd(3),
                newBaseDirectory = BaseDirectory.DirectoryFd(3),
            ),
        )
    }
}
