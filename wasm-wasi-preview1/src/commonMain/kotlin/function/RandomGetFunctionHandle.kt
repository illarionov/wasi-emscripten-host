/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasi.preview1.type.SizeType
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered

public class RandomGetFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.RANDOM_GET, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Byte::class) buf: WasmPtr,
        @SizeType bufLen: Size,
    ): Errno {
        return try {
            val entropyBytes = host.entropySource.generateEntropy(bufLen)
            check(entropyBytes.size == bufLen)

            memory.sinkWithMaxSize(buf, bufLen).buffered().use {
                it.write(entropyBytes)
            }
            Errno.SUCCESS
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            host.rootLogger.withTag("RandomGetFunctionHandle").i(e) {
                "Generate entropy failed"
            }
            Errno.INVAL
        }
    }
}
