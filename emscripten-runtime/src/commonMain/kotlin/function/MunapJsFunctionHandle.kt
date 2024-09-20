/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.EmscriptenHostFunction.MUNMAP_JS
import at.released.weh.emcripten.runtime.include.sys.SysMmanMapFlags
import at.released.weh.emcripten.runtime.include.sys.SysMmanProt
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.filesystem.common.Fd
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

public class MunapJsFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(MUNMAP_JS, host) {
    public fun execute(
        @IntWasmPtr(Byte::class) addr: WasmPtr,
        len: Int,
        @SysMmanProt prot: Int,
        @SysMmanMapFlags flags: Int,
        @Fd fd: Int,
        offset: Long,
    ): Int {
        logger.v {
            "munmapJs($addr, $len, 0x${prot.toString(16)}, 0x${flags.toString(16)}," +
                    " $fd, $offset): Not implemented"
        }
        return -Errno.INVAL.code // Not Supported
    }
}
