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
import at.released.weh.host.base.memory.ReadOnlyMemory
import at.released.weh.host.base.memory.readNullTerminatedString
import at.released.weh.host.emscripten.AssertionFailedException
import at.released.weh.host.emscripten.EmscriptenHostFunction

public class AssertFailFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(EmscriptenHostFunction.ASSERT_FAIL, host) {
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
