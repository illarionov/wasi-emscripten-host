/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LONG_NUMERICAL_VALUES_SEPARATED")

package at.released.weh.wasi.preview1.function

import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.model.Filetype.REGULAR_FILE
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.StructTimespec
import at.released.weh.filesystem.op.stat.timeNanos
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.test.fixtures.TestEmbedderHost
import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.wasi.preview1.ext.FILESTAT_PACKED_SIZE
import at.released.weh.wasi.preview1.ext.toVirtualPath
import at.released.weh.wasi.preview1.ext.writeFilesystemPath
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import at.released.weh.wasi.preview1.type.LookupflagsFlag.SYMLINK_FOLLOW
import at.released.weh.wasm.core.memory.sourceWithMaxSize
import at.released.weh.wasm.core.test.fixtures.TestMemory
import kotlinx.io.buffered
import kotlinx.io.readIntLe
import kotlinx.io.readLongLe
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class PathFilestatGetFunctionHandleTest {
    private val fileSystem = TestFileSystem()
    private val memory = TestMemory()
    private val host = TestEmbedderHost(
        fileSystem = fileSystem,
    )
    private val filestatGetFunctionHandle = PathFilestatGetFunctionHandle(host)

    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @AfterTest
    fun cleanup() {
        TestEnvironment.cleanup()
    }

    @Test
    fun path_filestat_get_success_case() {
        val testStructStat = StructStat(
            deviceId = 5,
            inode = 10,
            mode = 0b000_111_111_111,
            type = REGULAR_FILE,
            links = 1,
            usedId = 1001,
            groupId = 1002,
            specialFileDeviceId = 0,
            size = 11234,
            blockSize = 512,
            blocks = 22,
            accessTime = StructTimespec(1729711123, 0),
            modificationTime = StructTimespec(1729711120, 1),
            changeStatusTime = StructTimespec(1729711119, 2),
        )
        fileSystem.onOperation(Stat) { _ -> testStructStat.right() }

        val testPathAddr = 0x80
        val testPath = "testPath".toVirtualPath()
        val testPathBinarySize = memory.writeFilesystemPath(testPathAddr, testPath)

        val testAddr = 0x200
        val errNo = filestatGetFunctionHandle.execute(
            memory = memory,
            fd = 4,
            flags = SYMLINK_FOLLOW,
            path = testPathAddr,
            pathSize = testPathBinarySize,
            filestatAddr = testAddr,
        )

        assertThat(errNo).isEqualTo(SUCCESS)
        memory.sourceWithMaxSize(testAddr, FILESTAT_PACKED_SIZE).buffered().use { source ->
            assertThat(source.readLongLe()).isEqualTo(testStructStat.deviceId)
            assertThat(source.readLongLe()).isEqualTo(testStructStat.inode)
            assertThat(source.readIntLe()).isEqualTo(testStructStat.type.id)
            source.readIntLe() // Alignment
            assertThat(source.readLongLe()).isEqualTo(testStructStat.links)
            assertThat(source.readLongLe()).isEqualTo(testStructStat.size)
            assertThat(source.readLongLe()).isEqualTo(testStructStat.accessTime.timeNanos)
            assertThat(source.readLongLe()).isEqualTo(testStructStat.modificationTime.timeNanos)
            assertThat(source.readLongLe()).isEqualTo(testStructStat.changeStatusTime.timeNanos)
        }
    }
}
