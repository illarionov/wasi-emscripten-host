/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.module.emscripten.function

import at.released.weh.bindings.chasm.module.emscripten.EmscriptenHostFunctionHandle
import at.released.weh.host.base.function.HostFunction
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue

internal class NotImplementedEmscriptenFunction(
    private val function: HostFunction,
) : EmscriptenHostFunctionHandle {
    override fun invoke(args: List<ExecutionValue>): List<ExecutionValue> {
        error("Function `$function` not implemented")
    }
}
