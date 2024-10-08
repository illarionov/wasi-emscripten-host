/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasm.core.test.fixtures

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.readTo

internal class TestMemoryRawSink(
    private val memory: TestMemory,
    @IntWasmPtr baseAddr: WasmPtr,
    @IntWasmPtr val toAddrExclusive: WasmPtr,
) : RawSink {
    @IntWasmPtr
    public var baseAddr: WasmPtr = baseAddr
        private set

    public var isClosed: Boolean = false
        private set

    override fun write(source: Buffer, byteCount: Long) {
        require(byteCount >= 0) { "byteCount is negative" }
        check(!isClosed) { "Stream is closed" }

        val endAddrExclusive = baseAddr + byteCount
        require(endAddrExclusive <= toAddrExclusive) {
            "Cannot write `$byteCount` bytes to memory range $baseAddr ..<$toAddrExclusive: out of boundary access"
        }

        source.readTo(
            sink = memory.bytes,
            startIndex = baseAddr,
            endIndex = baseAddr + byteCount.toInt(),
        )
        baseAddr = endAddrExclusive.toInt()
    }

    override fun flush() = Unit

    override fun close() {
        isClosed = true
    }
}
