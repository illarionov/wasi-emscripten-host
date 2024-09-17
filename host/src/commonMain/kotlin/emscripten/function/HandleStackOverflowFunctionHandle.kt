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
import at.released.weh.host.emscripten.export.stack.EmscriptenStack

public class HandleStackOverflowFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.HANDLE_STACK_OVERFLOW, host) {
    public fun execute(
        stackBindings: EmscriptenStack,
        @IntWasmPtr(Byte::class) requestedSp: WasmPtr,
    ): Nothing {
        val base = stackBindings.emscriptenStackBase
        val end = stackBindings.emscriptenStackEnd
        error("Stack overflow (Attempt to set SP to $requestedSp, with stack limits [$end — $base])")
    }
}
