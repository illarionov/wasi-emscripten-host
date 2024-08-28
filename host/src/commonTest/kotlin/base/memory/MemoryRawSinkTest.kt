/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.tableOf
import at.released.weh.host.base.WasmPtr
import kotlinx.io.Buffer
import kotlin.test.Test
import kotlin.test.assertFailsWith

class MemoryRawSinkTest {
    @Test
    fun write_should_copy_bytes() {
        val writeByteTracker = WriteBytesToMemoryTracker()
        val sink = TestMemoryRawSink(
            baseAddr = WasmPtr<Unit>(100),
            toAddrExclusive = WasmPtr<Unit>(116),
            writeBytesToMemoryHandler = writeByteTracker,
        )
        val buffer = Buffer()
        sink.write(buffer, 4)
        sink.write(buffer, 8)
        sink.write(buffer, 4)

        assertThat(writeByteTracker.invocations)
            .containsExactly(
                WriteBytesInvocation(100, 4),
                WriteBytesInvocation(104, 8),
                WriteBytesInvocation(112, 4),
            )
    }

    @Test
    fun write_should_throw_iae_on_negative_byte_count() {
        val sink = TestMemoryRawSink(
            baseAddr = WasmPtr<Unit>(100),
            toAddrExclusive = WasmPtr<Unit>(116),
        )

        assertFailsWith<IllegalArgumentException> {
            sink.write(Buffer(), -4)
        }
    }

    @Test
    fun write_should_throw_ise_on_sink_is_closed() {
        val sink = TestMemoryRawSink(
            baseAddr = WasmPtr<Unit>(100),
            toAddrExclusive = WasmPtr<Unit>(116),
        )
        sink.close()

        assertFailsWith<IllegalStateException> {
            sink.write(Buffer(), 4)
        }
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun write_should_throw_ise_on_unknown_exception() {
        val sink = TestMemoryRawSink(
            baseAddr = WasmPtr<Unit>(100),
            toAddrExclusive = WasmPtr<Unit>(116),
            writeBytesToMemoryHandler = { _, _, _ -> throw RuntimeException("Test exception") },
        )
        assertFailsWith<IllegalStateException> {
            sink.write(Buffer(), 4)
        }
    }

    @Test
    fun write_should_throw_ise_on_out_of_bound_access() {
        tableOf("baseAddr", "toAddrExclusive", "writeBytes")
            .row(100, 100, 1)
            .row(100, 200, 101)
            .row(100, 200, 102)
            .forAll { baseAddr, toAddrExclusive, writeBytes ->
                val sink = TestMemoryRawSink(
                    baseAddr = WasmPtr<Unit>(baseAddr),
                    toAddrExclusive = WasmPtr<Unit>(toAddrExclusive),
                )
                assertFailsWith<IllegalArgumentException> {
                    sink.write(Buffer(), writeBytes.toLong())
                }
            }
    }

    @Test
    fun write_should_not_throw_on_zero_bytes_write() {
        val writeByteTracker = WriteBytesToMemoryTracker()
        val sink = TestMemoryRawSink(
            baseAddr = WasmPtr<Unit>(100),
            toAddrExclusive = WasmPtr<Unit>(104),
            writeBytesToMemoryHandler = writeByteTracker,
        )
        val buffer = Buffer()
        sink.write(buffer, 4)
        sink.write(buffer, 0)

        assertThat(writeByteTracker.invocations)
            .containsExactly(
                WriteBytesInvocation(100, 4),
                WriteBytesInvocation(104, 0),
            )
    }

    @Test
    fun flush_should_throw_ise_on_closed_sink() {
        val sink = TestMemoryRawSink(
            baseAddr = WasmPtr<Unit>(100),
            toAddrExclusive = WasmPtr<Unit>(116),
        )
        sink.close()

        assertFailsWith<IllegalStateException> {
            sink.flush()
        }
    }

    @Test
    fun close_is_safe_to_call_more_than_once() {
        val sink = TestMemoryRawSink(
            baseAddr = WasmPtr<Unit>(100),
            toAddrExclusive = WasmPtr<Unit>(116),
        )
        sink.close()
        sink.close()
    }

    private class TestMemoryRawSink(
        baseAddr: WasmPtr<*>,
        toAddrExclusive: WasmPtr<*>,
        val writeBytesToMemoryHandler: (buffer: Buffer, fromAddr: WasmPtr<*>, byteCount: Long) -> Unit = { _, _, _ -> },
    ) : MemoryRawSink(baseAddr, toAddrExclusive) {
        override fun writeBytesToMemory(source: Buffer, toAddr: WasmPtr<*>, byteCount: Long) {
            writeBytesToMemoryHandler(source, toAddr, byteCount)
        }
    }

    class WriteBytesToMemoryTracker(
        val invocations: MutableList<WriteBytesInvocation> = mutableListOf(),
    ) : (Buffer, WasmPtr<*>, Long) -> Unit {
        override fun invoke(buffer: Buffer, fromAddr: WasmPtr<*>, toAddr: Long) {
            invocations.add(WriteBytesInvocation(fromAddr.addr, toAddr))
        }
    }

    data class WriteBytesInvocation(
        val toAddr: Int,
        val byteCount: Long,
    )
}
