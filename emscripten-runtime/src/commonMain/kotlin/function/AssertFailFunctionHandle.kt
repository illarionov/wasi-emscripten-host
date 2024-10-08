/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.function

import at.released.weh.emcripten.runtime.AssertionFailedException
import at.released.weh.emcripten.runtime.EmscriptenHostFunction.ASSERT_FAIL
import at.released.weh.host.EmbedderHost
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.ReadOnlyMemory
import at.released.weh.wasm.core.memory.readNullTerminatedString

public class AssertFailFunctionHandle(
    host: EmbedderHost,
) : EmscriptenHostFunctionHandle(ASSERT_FAIL, host) {
    public fun execute(
        memory: ReadOnlyMemory,
        @IntWasmPtr(Byte::class) condition: WasmPtr,
        @IntWasmPtr(Byte::class) filename: WasmPtr,
        line: Int,
        @IntWasmPtr(Byte::class) func: WasmPtr,
    ): Nothing {
        throw AssertionFailedException(
            condition = memory.readNullTerminatedString(condition),
            filename = memory.readNullTerminatedString(filename),
            line = line,
            func = memory.readNullTerminatedString(func),
        )
    }
}
