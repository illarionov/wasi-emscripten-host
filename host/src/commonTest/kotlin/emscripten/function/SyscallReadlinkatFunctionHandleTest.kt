/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.writeNullTerminatedString
import at.released.weh.host.base.plus
import at.released.weh.host.include.Fcntl
import at.released.weh.host.test.assertions.byteAt
import at.released.weh.host.test.assertions.hasBytesAt
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.host.test.fixtures.TestMemory
import at.released.weh.test.io.bootstrap.TestEnvironment
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SyscallReadlinkatFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val host = TestEmbedderHost(fileSystem = fileSystem)
    private val memory = TestMemory(fileSystem = host.fileSystem)
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
        val expectedLinkTarget = "usr/sbin"
        val expectedLinkTargetBytes = expectedLinkTarget.encodeToByteArray()

        fileSystem.onOperation(ReadLink) { operation ->
            assertThat(operation.path).isEqualTo("/sbin")
            expectedLinkTarget.right()
        }
        val pathnamePtr: WasmPtr<Byte> = WasmPtr(64)
        val bufPtr: WasmPtr<Byte> = WasmPtr(128)

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
        val pathnamePtr = WasmPtr<Byte>(64).also {
            memory.writeNullTerminatedString(it, "")
        }

        val sizeOrErrno = readlinkatFunctionHandle.execute(
            memory = memory,
            rawDirFd = Fcntl.AT_FDCWD,
            pathnamePtr = pathnamePtr,
            buf = WasmPtr(128),
            bufSize = -1,
        )

        assertThat(sizeOrErrno).isEqualTo(-Errno.INVAL.code)
    }

    @Test
    fun readlinkAt_should_return_negative_error_code_on_filesystem_error() {
        val pathnamePtr = WasmPtr<Byte>(64).also {
            memory.writeNullTerminatedString(it, "/")
        }
        fileSystem.onOperation(ReadLink) { _ ->
            AccessDenied("Test access denied").left()
        }

        val sizeOrErrno = readlinkatFunctionHandle.execute(
            memory = memory,
            rawDirFd = Fcntl.AT_FDCWD,
            pathnamePtr = pathnamePtr,
            buf = WasmPtr(128),
            bufSize = 100,
        )

        assertThat(sizeOrErrno).isEqualTo(-Errno.ACCES.code)
    }

    @Test
    fun readlinkAt_should_not_exceed_bufsize_limit() {
        fileSystem.onOperation(ReadLink) {
            "usr/sbin".right()
        }

        val pathnamePtr: WasmPtr<Byte> = WasmPtr(64)
        val bufPtr: WasmPtr<Byte> = WasmPtr(128)

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
