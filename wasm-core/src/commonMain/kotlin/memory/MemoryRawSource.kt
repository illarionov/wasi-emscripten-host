/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasm.core.memory

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import kotlinx.io.Buffer
import kotlinx.io.RawSource

public abstract class MemoryRawSource(
    @IntWasmPtr protected var baseAddr: WasmPtr,
    @IntWasmPtr protected val toAddrExclusive: WasmPtr,
) : RawSource {
    private var isClosed: Boolean = false
    protected val bytesLeft: Long
        get() = (toAddrExclusive - baseAddr).toLong()

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        require(byteCount >= 0) { "byteCount is negative" }
        check(!isClosed) { "Stream is closed" }

        val bytesLeft: Long = (toAddrExclusive - baseAddr).toLong()
        val readBytes = byteCount.coerceAtMost(bytesLeft).toInt()
        if (readBytes <= 0) {
            return -1
        }

        try {
            readBytesFromMemory(baseAddr, sink, readBytes)
        } catch (ise: IllegalStateException) {
            throw ise
        } catch (iae: IllegalArgumentException) {
            throw iae
        } catch (@Suppress("TooGenericExceptionCaught") ex: Throwable) {
            throw IllegalStateException(ex.message, ex)
        }

        baseAddr += readBytes
        return readBytes.toLong()
    }

    protected abstract fun readBytesFromMemory(
        @IntWasmPtr srcAddr: WasmPtr,
        sink: Buffer,
        readBytes: Int,
    )

    override fun close() {
        isClosed = true
    }
}
