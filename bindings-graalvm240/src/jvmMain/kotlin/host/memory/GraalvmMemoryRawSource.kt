/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.memory

import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.memory.MemoryRawSource
import com.oracle.truffle.api.nodes.Node
import kotlinx.io.Buffer
import kotlinx.io.asOutputStream
import org.graalvm.wasm.memory.WasmMemory
import java.io.IOException

internal class GraalvmMemoryRawSource(
    private val memoryProvider: () -> WasmMemory,
    baseAddr: WasmPtr<*>,
    toAddrExclusive: WasmPtr<*>,
    private val node: Node?,
) : MemoryRawSource(baseAddr, toAddrExclusive) {
    override fun readBytesFromMemory(srcAddr: WasmPtr<*>, sink: Buffer, readBytes: Int) {
        val outputStream = sink.asOutputStream()
        try {
            memoryProvider().copyToStream(node, outputStream, srcAddr.addr, readBytes)
        } catch (ioe: IOException) {
            throw IllegalStateException("Can not read from memory", ioe)
        } finally {
            outputStream.flush()
        }
    }
}
