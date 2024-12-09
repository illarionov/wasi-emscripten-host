/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.clock.Clock
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.ext.toVirtualPath
import at.released.weh.wasi.preview1.ext.writeFilesystemPath
import at.released.weh.wasi.preview1.type.Errno.INVAL
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasi.preview1.type.FstflagsFlag
import at.released.weh.wasi.preview1.type.LookupflagsFlag.SYMLINK_FOLLOW
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlin.experimental.or
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class PathFilestatSetTimesFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val memory = TestMemory()
    private val currentTime = 1_730_476_887.seconds.inWholeNanoseconds
    private val host = TestEmbedderHost(
        fileSystem = fileSystem,
        clock = object : Clock {
            override fun getCurrentTimeEpochNanoseconds(): Long = currentTime
            override fun getResolutionNanoseconds(): Long = 1.milliseconds.inWholeNanoseconds
        },
    )
    private val filestatSetTimesFunctionHandle = PathFilestatSetTimesFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun path_filestat_set_times_success_case() {
        var timestampRequest: SetTimestamp? = null
        fileSystem.onOperation(SetTimestamp) { request ->
            timestampRequest = request
            Unit.right()
        }

        val testPathAddr = 0x80
        val testPath = "testPath".toVirtualPath()
        val testPathBinarySize = memory.writeFilesystemPath(testPathAddr, testPath)

        val errNo = filestatSetTimesFunctionHandle.execute(
            memory = memory,
            fd = 4,
            flags = SYMLINK_FOLLOW,
            pathAddr = testPathAddr,
            pathSize = testPathBinarySize,
            atime = 1242L,
            mtime = 1244L,
            fstflags = FstflagsFlag.ATIM_NOW or FstflagsFlag.MTIM,
        )
        assertThat(errNo).isEqualTo(SUCCESS)
        assertThat(timestampRequest).isEqualTo(
            SetTimestamp(
                path = testPath.toString(),
                baseDirectory = BaseDirectory.DirectoryFd(4),
                atimeNanoseconds = currentTime,
                mtimeNanoseconds = 1244L,
                followSymlinks = true,
            ),
        )
    }

    @Test
    fun path_filestat_should_fail_on_incorrect_fstflags() {
        fileSystem.onOperation(SetTimestamp) { _ -> Unit.right() }

        val testPathAddr = 0x80
        val testPath = "testPath".toVirtualPath()
        val testPathBinarySize = memory.writeFilesystemPath(testPathAddr, testPath)

        tableOf("fstflags")
            .row(FstflagsFlag.ATIM_NOW or FstflagsFlag.ATIM)
            .row(FstflagsFlag.MTIM_NOW or FstflagsFlag.MTIM)
            .forAll { fstflags ->
                val errNo = filestatSetTimesFunctionHandle.execute(
                    memory = memory,
                    fd = 4,
                    flags = SYMLINK_FOLLOW,
                    pathAddr = testPathAddr,
                    pathSize = testPathBinarySize,
                    atime = 1242L,
                    mtime = 1244L,
                    fstflags = fstflags,
                )
                assertThat(errNo).isEqualTo(INVAL)
            }
    }
}
