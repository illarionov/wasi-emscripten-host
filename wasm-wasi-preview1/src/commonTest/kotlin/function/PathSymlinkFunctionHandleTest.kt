/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")

package at.released.weh.wasi.preview1.function

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.ext.writeFilesystemPath
import at.released.weh.wasi.preview1.type.Errno.NOENT
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PathSymlinkFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val memory = TestMemory()
    private val host = TestEmbedderHost(
        fileSystem = fileSystem,
    )
    private val pathSymlinkFunctionHandle = PathSymlinkFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun path_symlink_success_case() {
        var symlinkRequest: Symlink? = null
        fileSystem.onOperation(Symlink) { request ->
            symlinkRequest = request
            Unit.right()
        }

        val oldPath = "../target"
        val newPath = "preopened/newPath"

        val oldpathAddr = 0x10
        val oldPathBinarySize = memory.writeFilesystemPath(oldpathAddr, oldPath)

        val newPathAddr = 0x40
        val newPathBinarySize = memory.writeFilesystemPath(newPathAddr, newPath)

        val errNo = pathSymlinkFunctionHandle.execute(
            memory,
            oldpathAddr,
            oldPathBinarySize,
            4,
            newPathAddr,
            newPathBinarySize,
        )
        assertThat(errNo).isEqualTo(SUCCESS)
        assertThat(symlinkRequest).isEqualTo(
            Symlink(
                oldPath = oldPath,
                newPath = newPath,
                newPathBaseDirectory = BaseDirectory.DirectoryFd(4),
                allowAbsoluteOldPath = false,
            ),
        )
    }

    @Test
    fun path_symlink_should_fail_with_trailing_slash() {
        fileSystem.onOperation(Symlink) { _ -> Unit.right() }

        val oldPath = "../target"
        val newPath = "preopened/newPath/"

        val oldpathAddr = 0x10
        val oldPathBinarySize = memory.writeFilesystemPath(oldpathAddr, oldPath)

        val newPathAddr = 0x40
        val newPathBinarySize = memory.writeFilesystemPath(newPathAddr, newPath)

        val errNo = pathSymlinkFunctionHandle.execute(
            memory,
            oldpathAddr,
            oldPathBinarySize,
            4,
            newPathAddr,
            newPathBinarySize,
        )
        assertThat(errNo).isEqualTo(NOENT)
    }
}
