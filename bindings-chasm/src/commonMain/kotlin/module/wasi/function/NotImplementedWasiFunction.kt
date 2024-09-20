/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.wasi.function

import at.released.weh.bindings.chasm.module.wasi.WasiHostFunctionHandle
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasm.core.HostFunction
import io.github.charlietap.chasm.embedding.shapes.Value

internal class NotImplementedWasiFunction(
    private val function: HostFunction,
) : WasiHostFunctionHandle {
    override operator fun invoke(args: List<Value>): Errno {
        error("Function `$function` not implemented")
    }
}
