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
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.ext.toVirtualPath
import at.released.weh.wasi.preview1.ext.writeFilesystemPath
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasm.core.test.assertions.byteAt
import at.released.weh.wasm.core.test.assertions.hasBytesAt
import at.released.weh.wasm.core.test.fixtures.TestMemory
import at.released.weh.wasm.core.test.fixtures.TestMemory.Companion.MEMORY_FILL_BYTE
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PathReadlinkFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val memory = TestMemory()
    private val host = TestEmbedderHost(
        fileSystem = fileSystem,
    )
    private val pathReadlinkFunctionHandle = PathReadlinkFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun path_readlink_success_case() {
        val targetValue = "../target".toVirtualPath()
        fileSystem.onOperation(ReadLink) { _: ReadLink -> targetValue.right() }

        val linkpathAddr = 0x80
        val linkPath = "linktPath".toVirtualPath()
        val linkPathBinarySize = memory.writeFilesystemPath(linkpathAddr, linkPath)

        val bufAddr = 0x100
        val bufLen = 10

        val sizeAddr = 0x110
        val errNo = pathReadlinkFunctionHandle.execute(
            memory = memory,
            fd = 3,
            pathAddr = linkpathAddr,
            pathSize = linkPathBinarySize,
            bufAddr = bufAddr,
            bufLen = bufLen,
            sizeAddr = sizeAddr,
        )
        assertThat(errNo).isEqualTo(SUCCESS)
        assertThat(memory.readI32(sizeAddr)).isEqualTo(targetValue.utf8SizeBytes)
        assertThat(memory).hasBytesAt(bufAddr, targetValue.utf8Bytes.toByteArray())
        assertThat(memory).byteAt(bufAddr + targetValue.utf8SizeBytes).isEqualTo(MEMORY_FILL_BYTE)
    }

    @Test
    fun path_readlink_should_truncate_text_on_no_buffer() {
        val targetValue = "../target".toVirtualPath()
        fileSystem.onOperation(ReadLink) { _: ReadLink -> targetValue.right() }

        val linkpathAddr = 0x80
        val linkPath = "linkPath".toVirtualPath()
        val linkPathBinarySize = memory.writeFilesystemPath(linkpathAddr, linkPath)
        val bufAddr = 0x100
        val bufLen = 4

        val sizeAddr = 0x110
        val errNo = pathReadlinkFunctionHandle.execute(
            memory = memory,
            fd = 3,
            pathAddr = linkpathAddr,
            pathSize = linkPathBinarySize,
            bufAddr = bufAddr,
            bufLen = bufLen,
            sizeAddr = sizeAddr,
        )
        assertThat(errNo).isEqualTo(SUCCESS)
        assertThat(memory.readI32(sizeAddr)).isEqualTo(bufLen)
        assertThat(memory).hasBytesAt(bufAddr, "../t".encodeToByteArray())
        assertThat(memory).byteAt(bufAddr + 4).isEqualTo(MEMORY_FILL_BYTE)
    }
}
