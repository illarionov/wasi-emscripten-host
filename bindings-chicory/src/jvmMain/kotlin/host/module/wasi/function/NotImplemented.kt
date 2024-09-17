/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.wasi.function

import at.released.weh.bindings.chicory.host.module.wasi.WasiHostFunctionHandle
import at.released.weh.bindings.chicory.host.module.wasi.WasiHostFunctionHandleFactory
import at.released.weh.wasi.filesystem.common.Errno
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal val notImplementedWasiHostFunctionHandleFactory: WasiHostFunctionHandleFactory = { _, _ -> NotImplemented }

internal object NotImplemented : WasiHostFunctionHandle {
    override fun apply(instance: Instance, vararg args: Value): Errno {
        error("Function not implemented")
    }
}
