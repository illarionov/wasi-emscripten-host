/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.GETENTROPY
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

public class GetentropyFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(GETENTROPY, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Byte::class) buffer: WasmPtr,
        size: Int,
    ): Int {
        return try {
            val entropyBytes = host.entropySource.generateEntropy(size)
            check(entropyBytes.size == size)

            memory.sinkWithMaxSize(buffer, size).buffered().use {
                it.write(entropyBytes)
            }
            0
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.e(e) { "getentropy() failed" }
            -1
        }
    }
}
