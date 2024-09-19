/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.MMAP_JS
import at.released.weh.host.EmbedderHost
import at.released.weh.host.include.sys.SysMmanMapFlags
import at.released.weh.host.include.sys.SysMmanProt
import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasi.filesystem.common.Fd
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

public class MmapJsFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(MMAP_JS, host) {
    public fun execute(
        len: Int,
        @SysMmanProt prot: Int,
        @SysMmanMapFlags flags: Int,
        @Fd fd: Int,
        offset: Long,
        @IntWasmPtr(Int::class) pAllocated: WasmPtr,
        @IntWasmPtr(WasmPtr::class) pAddr: WasmPtr, // **byte
    ): Int {
        logger.v {
            "mmapJs($fd, $len, 0x${prot.toString(16)}, 0x${flags.toString(16)}, $fd, " +
                    "$offset, $pAllocated, $pAddr): Not implemented"
        }
        return -Errno.INVAL.code // Not Supported
    }
}
