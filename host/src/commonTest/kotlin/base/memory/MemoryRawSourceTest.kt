/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import kotlinx.io.Buffer
import kotlin.test.Test
import kotlin.test.assertFailsWith

class MemoryRawSourceTest {
    @Test
    fun readAtMostTo_should_copy_bytes() {
        val readBytesTracker = ReadBytesToMemoryTracker()
        val sink = TestMemoryRawSource(
            baseAddr = 100,
            toAddrExclusive = 116,
            readBytesHandler = readBytesTracker,
        )
        val buffer = Buffer()
        val readBytes = listOf(
            sink.readAtMostTo(buffer, 4),
            sink.readAtMostTo(buffer, 8),
            sink.readAtMostTo(buffer, 4),
        )

        assertThat(readBytes).containsExactly(4L, 8L, 4L)

        assertThat(readBytesTracker.invocations)
            .containsExactly(
                ReadBytesInvocation(100, 4),
                ReadBytesInvocation(104, 8),
                ReadBytesInvocation(112, 4),
            )
    }

    @Test
    fun readAtMostTo_should_throw_iae_on_negative_byte_count() {
        val sink = TestMemoryRawSource(
            baseAddr = 100,
            toAddrExclusive = 116,
        )

        assertFailsWith<IllegalArgumentException> {
            sink.readAtMostTo(Buffer(), -4)
        }
    }

    @Test
    fun readAtMostTo_should_throw_ise_when_sink_is_closed() {
        val sink = TestMemoryRawSource(
            baseAddr = 100,
            toAddrExclusive = 116,
        )
        sink.close()

        assertFailsWith<IllegalStateException> {
            sink.readAtMostTo(Buffer(), 4)
        }
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun readAtMostTo_should_throw_ise_on_unknown_exception() {
        val sink = TestMemoryRawSource(
            baseAddr = 100,
            toAddrExclusive = 116,
            readBytesHandler = { _, _, _ -> throw RuntimeException("Test exception") },
        )
        assertFailsWith<IllegalStateException> {
            sink.readAtMostTo(Buffer(), 4)
        }
    }

    @Test
    fun readAtMostTo_should_return_correct_code_on_exhausted() {
        tableOf("baseAddr", "toAddrExclusive", "readBytes", "expectedResult")
            .row(100, 100, 1, -1L)
            .row(100, 200, 101, 100L)
            .row(100, 200, 102, 100L)
            .forAll { baseAddr, toAddrExclusive, readBytes, expectedResult ->
                val sink = TestMemoryRawSource(
                    baseAddr = baseAddr,
                    toAddrExclusive = toAddrExclusive,
                )
                val result: Long = sink.readAtMostTo(Buffer(), readBytes.toLong())
                assertThat(result).isEqualTo(expectedResult)
            }
    }

    @Test
    fun readAtMostTo_should_return_correct_code_on_partial_exhausted() {
        val sink = TestMemoryRawSource(
            baseAddr = 100,
            toAddrExclusive = 116,
        )
        val read1 = sink.readAtMostTo(Buffer(), 10)
        assertThat(read1).isEqualTo(10)

        val read2 = sink.readAtMostTo(Buffer(), 10)
        assertThat(read2).isEqualTo(6)

        val read3 = sink.readAtMostTo(Buffer(), 10)
        assertThat(read3).isEqualTo(-1)
    }

    @Test
    fun readAtMostTo_should_return_correct_code_on_zero_bytes_read() {
        val sink = TestMemoryRawSource(
            baseAddr = 100,
            toAddrExclusive = 116,
        )
        val read = sink.readAtMostTo(Buffer(), 0)
        assertThat(read).isEqualTo(-1)
    }

    @Test
    fun close_is_safe_to_call_more_than_once() {
        val sink = TestMemoryRawSource(
            baseAddr = 100,
            toAddrExclusive = 116,
        )
        sink.close()
        sink.close()
    }

    private class TestMemoryRawSource(
        @IntWasmPtr baseAddr: Int,
        @IntWasmPtr toAddrExclusive: Int,
        val readBytesHandler: (srcAddr: WasmPtr, sink: Buffer, readBytes: Int) -> Unit = { _, _, _ -> },
    ) : MemoryRawSource(baseAddr, toAddrExclusive) {
        override fun readBytesFromMemory(srcAddr: WasmPtr, sink: Buffer, readBytes: Int) {
            readBytesHandler(srcAddr, sink, readBytes)
        }
    }

    class ReadBytesToMemoryTracker(
        val invocations: MutableList<ReadBytesInvocation> = mutableListOf(),
    ) : (WasmPtr, Buffer, Int) -> Unit {
        override fun invoke(srcAddr: WasmPtr, sink: Buffer, readBytes: Int) {
            invocations.add(ReadBytesInvocation(srcAddr, readBytes))
        }
    }

    data class ReadBytesInvocation(
        val srcAddr: Int,
        val byteCount: Int,
    )
}
