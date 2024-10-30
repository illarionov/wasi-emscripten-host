/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.preopened.VirtualPath
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize

internal fun Memory.writeFilesystemPath(
    @IntWasmPtr addr: WasmPtr,
    path: VirtualPath,
): Int {
    val buffer = path.encodeToBuffer()
    val newPathBinarySize = buffer.size.toInt()
    sinkWithMaxSize(addr, newPathBinarySize).use {
        it.write(buffer, newPathBinarySize.toLong())
    }
    return newPathBinarySize
}