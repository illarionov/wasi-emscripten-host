/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Ciovec
import at.released.weh.wasi.preview1.type.CiovecArray
import at.released.weh.wasi.preview1.type.Iovec
import at.released.weh.wasi.preview1.type.IovecArray
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readPtr

internal fun readCiovecs(
    memory: ReadOnlyMemory,
    @IntWasmPtr(Ciovec::class) pCiov: WasmPtr,
    ciovCnt: Int,
): CiovecArray {
    val iovecs = MutableList(ciovCnt) { idx ->
        val pCiovec: WasmPtr = pCiov + 8 * idx
        Ciovec(
            buf = memory.readPtr(pCiovec),
            bufLen = memory.readI32(pCiovec + 4),
        )
    }
    return iovecs
}

internal fun readIovecs(
    memory: ReadOnlyMemory,
    @IntWasmPtr(Iovec::class) pIov: WasmPtr,
    iovCnt: Int,
): IovecArray {
    val iovecs = MutableList(iovCnt) { idx ->
        val pIovec: WasmPtr = pIov + 8 * idx
        Iovec(
            buf = memory.readPtr(pIovec),
            bufLen = memory.readI32(pIovec + 4),
        )
    }
    return iovecs
}
