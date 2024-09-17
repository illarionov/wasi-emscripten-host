/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Fd
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction
import at.released.weh.host.include.sys.SysMmanMapFlags
import at.released.weh.host.include.sys.SysMmanProt

public class MmapJsFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.MMAP_JS, host) {
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
