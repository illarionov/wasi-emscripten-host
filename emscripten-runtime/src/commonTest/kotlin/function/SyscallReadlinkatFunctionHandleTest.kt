/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")

package at.released.weh.emcripten.runtime.function

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.emcripten.runtime.include.Fcntl
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.writeNullTerminatedString
import at.released.weh.wasm.core.test.assertions.byteAt
import at.released.weh.wasm.core.test.assertions.hasBytesAt
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SyscallReadlinkatFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val host = TestEmbedderHost(fileSystem = fileSystem)
    private val memory = TestMemory()
    private val readlinkatFunctionHandle = SyscallReadlinkatFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun readlinkAt_test_success_case() {
        val expectedLinkTarget = "usr/sbin".toVirtualPath()
        val expectedLinkTargetBytes = expectedLinkTarget.utf8Bytes.toByteArray()

        fileSystem.onOperation(ReadLink) { operation ->
            assertThat(operation.path).isEqualTo("/sbin".toVirtualPath())
            expectedLinkTarget.right()
        }
        @IntWasmPtr(Byte::class)
        val pathnamePtr: WasmPtr = 64

        @IntWasmPtr(Byte::class)
        val bufPtr: WasmPtr = 128

        memory.fill(0xff.toByte())
        memory.writeNullTerminatedString(pathnamePtr, "/sbin")

        val size = readlinkatFunctionHandle.execute(
            memory = memory,
            rawDirFd = Fcntl.AT_FDCWD,
            pathnamePtr = pathnamePtr,
            buf = bufPtr,
            bufSize = 100,
        )

        assertThat(size).isEqualTo(expectedLinkTargetBytes.size)
        assertThat(memory).hasBytesAt(bufPtr, expectedLinkTargetBytes)

        // readlinkAt does not append a terminating null byte
        assertThat(memory).byteAt(bufPtr + expectedLinkTargetBytes.size).isEqualTo(0xff.toByte())
    }

    @Test
    fun readlinkAt_should_return_einval_on_incorrect_bufsize() {
        val pathnamePtr = 64.also {
            memory.writeNullTerminatedString(it, "")
        }

        val sizeOrErrno = readlinkatFunctionHandle.execute(
            memory = memory,
            rawDirFd = Fcntl.AT_FDCWD,
            pathnamePtr = pathnamePtr,
            buf = 128,
            bufSize = -1,
        )

        assertThat(sizeOrErrno).isEqualTo(-Errno.INVAL.code)
    }

    @Test
    fun readlinkAt_should_return_negative_error_code_on_filesystem_error() {
        val pathnamePtr = 64.also {
            memory.writeNullTerminatedString(it, "/")
        }
        fileSystem.onOperation(ReadLink) { _ ->
            AccessDenied("Testing access denied").left()
        }

        val sizeOrErrno = readlinkatFunctionHandle.execute(
            memory = memory,
            rawDirFd = Fcntl.AT_FDCWD,
            pathnamePtr = pathnamePtr,
            buf = 128,
            bufSize = 100,
        )

        assertThat(sizeOrErrno).isEqualTo(-Errno.ACCES.code)
    }

    @Test
    fun readlinkAt_should_not_exceed_bufsize_limit() {
        fileSystem.onOperation(ReadLink) {
            "usr/sbin".toVirtualPath().right()
        }

        @IntWasmPtr(Byte::class)
        val pathnamePtr: WasmPtr = 6

        @IntWasmPtr(Byte::class)
        val bufPtr: WasmPtr = 128

        memory.fill(0xff.toByte())
        memory.writeNullTerminatedString(pathnamePtr, "sbin")

        val sizeOrErrno = readlinkatFunctionHandle.execute(
            memory = memory,
            rawDirFd = Fcntl.AT_FDCWD,
            pathnamePtr = pathnamePtr,
            buf = bufPtr,
            bufSize = 1,
        )

        assertThat(sizeOrErrno).isEqualTo(1)
        assertThat(memory).hasBytesAt(bufPtr, byteArrayOf('u'.code.toByte(), 0xff.toByte()))
    }
}
