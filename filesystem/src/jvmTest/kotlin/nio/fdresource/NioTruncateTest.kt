/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.fdresource

import assertk.assertThat
import assertk.assertions.isEqualTo
import at.released.weh.filesystem.fdresource.nio.MAX_BUF_SIZE
import at.released.weh.filesystem.fdresource.nio.NioFileChannel
import at.released.weh.filesystem.fdresource.nio.truncate
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import kotlin.test.fail

class NioTruncateTest {
    @JvmField
    @Rule
    var folder: TemporaryFolder = TemporaryFolder()

    @Test
    fun test_extend_to_1_byte() {
        val testFile = createTestFile(size = 100)
        testFile.channel.position(51)
        testFile.truncate(101).onLeft { fail("extend() failed") }

        testFile.channel.use {
            assertThat(it.position()).isEqualTo(51)
            assertThat(it.size()).isEqualTo(101)
            it.assertBytesFromPositionAreZero(100)
        }
    }

    @Test
    fun test_do_not_extend() {
        val testFile = createTestFile(size = 100)
        testFile.channel.position(51)
        testFile.truncate(100).onLeft { fail("extend() failed") }

        testFile.channel.use {
            assertThat(it.position()).isEqualTo(51)
            assertThat(it.size()).isEqualTo(100)
        }
    }

    @Test
    fun test_extend_max_buf_size() {
        val testFile = createTestFile(size = 100)
        val newSize = 100 + MAX_BUF_SIZE
        testFile.channel.position(51)
        testFile.truncate(newSize).onLeft { fail("extend() failed") }

        testFile.channel.use {
            assertThat(it.position()).isEqualTo(51)
            assertThat(it.size()).isEqualTo(newSize)
            it.assertBytesFromPositionAreZero(100)
        }
    }

    @Test
    fun test_extend_large_extend() {
        val testFile = createTestFile(size = 100)
        val newSize = 100 + 3 * MAX_BUF_SIZE + 5
        testFile.channel.position(51)
        testFile.truncate(newSize).onLeft { fail("extend() failed") }

        testFile.channel.use {
            assertThat(it.position()).isEqualTo(51)
            assertThat(it.size()).isEqualTo(newSize)
            it.assertBytesFromPositionAreZero(100)
        }
    }

    private fun createTestFile(
        size: Long = 100,
        initialPosition: Long = 51,
        name: String = "testfile",
    ): NioFileChannel {
        val path: Path = folder.newFile(name).toPath()
        val channel = FileChannel.open(path, WRITE, CREATE, READ).apply {
            write(ByteBuffer.allocate(size.toInt()))
        }
        channel.position(initialPosition)
        return NioFileChannel(
            path = path,
            channel = channel,
            fdFlags = 0,
        )
    }

    private fun FileChannel.assertBytesFromPositionAreZero(
        startPosition: Long,
    ) {
        val buf = ByteBuffer.allocate(1024 * 1024)
        var position = startPosition
        val endPosition = this.size()
        while (position < endPosition) {
            buf.rewind()
            if (this.read(buf, position) < 0) {
                fail("Position outside of file range")
            }
            buf.flip()
            for (i in 0 until buf.limit()) {
                assertThat(buf.get(i)).isEqualTo(0)
            }
            position += buf.limit()
        }
    }
}
