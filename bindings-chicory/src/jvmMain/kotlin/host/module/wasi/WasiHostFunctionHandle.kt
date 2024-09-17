/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.host.module.wasi

import at.released.weh.wasi.filesystem.common.Errno
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.types.Value

internal fun interface WasiHostFunctionHandle {
    fun apply(instance: Instance, vararg args: Value): Errno
}
