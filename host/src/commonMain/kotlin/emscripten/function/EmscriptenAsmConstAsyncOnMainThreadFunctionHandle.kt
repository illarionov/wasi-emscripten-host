/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.emscripten.function

import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.emscripten.EmscriptenHostFunction

public class EmscriptenAsmConstAsyncOnMainThreadFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.EMSCRIPTEN_ASM_CONST_ASYNC_ON_MAIN_THREAD, host) {
    public fun execute(
        @IntWasmPtr(Byte::class) emAsmAddr: WasmPtr,
        @IntWasmPtr(Byte::class) sigPtr: WasmPtr,
        @IntWasmPtr(Byte::class) argbuf: WasmPtr,
    ) {
        logger.i {
            "emscripten_asm_const_async_on_main_thread($emAsmAddr, $sigPtr, $argbuf): " +
                    "Not implemented"
        }
    }
}
