/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import kotlinx.io.buffered
import kotlinx.io.write

internal fun Memory.writeFilesystemPath(
    @IntWasmPtr addr: WasmPtr,
    path: VirtualPath,
): Int {
    val newPathBinarySize = path.utf8SizeBytes
    sinkWithMaxSize(addr, newPathBinarySize).buffered().use {
        it.write(path.utf8)
    }
    return newPathBinarySize
}
