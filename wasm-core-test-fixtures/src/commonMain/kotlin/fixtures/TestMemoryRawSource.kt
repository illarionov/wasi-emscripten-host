/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasm.core.test.fixtures

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.MemoryRawSource
import kotlinx.io.Buffer

public class TestMemoryRawSource(
    private val memory: TestMemory,
    @IntWasmPtr baseAddr: WasmPtr,
    @IntWasmPtr toAddrExclusive: WasmPtr,
) : MemoryRawSource(baseAddr, toAddrExclusive) {
    override fun readBytesFromMemory(@IntWasmPtr srcAddr: WasmPtr, sink: Buffer, readBytes: Int) {
        sink.write(
            source = memory.bytes,
            startIndex = srcAddr,
            endIndex = srcAddr + readBytes,
        )
        sink.emit()
    }
}
