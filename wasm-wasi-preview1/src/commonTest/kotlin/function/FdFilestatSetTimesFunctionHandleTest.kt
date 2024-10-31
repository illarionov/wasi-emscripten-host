/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.op.settimestamp.SetTimestampFd
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.clock.Clock
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasi.preview1.type.FstflagsFlag
import kotlin.experimental.or
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class FdFilestatSetTimesFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val currentTime = 1_730_476_887.seconds.inWholeNanoseconds
    private val host = TestEmbedderHost(
        fileSystem = fileSystem,
        clock = object : Clock {
            override fun getCurrentTimeEpochNanoseconds(): Long = currentTime
            override fun getResolutionNanoseconds(): Long = 1.milliseconds.inWholeNanoseconds
        },
    )
    private val fdFilestatSetTimesFunctionHandle = FdFilestatSetTimesFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun fd_filestat_set_times_success_case() {
        var timestampRequest: SetTimestampFd? = null
        fileSystem.onOperation(SetTimestampFd) { request ->
            timestampRequest = request
            Unit.right()
        }

        val errNo = fdFilestatSetTimesFunctionHandle.execute(
            fd = 4,
            atime = 1242L,
            mtime = 1244L,
            fstflags = FstflagsFlag.ATIM_NOW or FstflagsFlag.MTIM,
        )
        assertThat(errNo).isEqualTo(SUCCESS)
        assertThat(timestampRequest).isEqualTo(
            SetTimestampFd(
                atimeNanoseconds = currentTime,
                mtimeNanoseconds = 1244L,
                fd = 4,
            ),
        )
    }
}
