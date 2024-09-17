/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.function

import at.released.weh.wasm.core.WasmValueType

public interface HostFunction {
    public val wasmName: String
    public val type: HostFunctionType

    public interface HostFunctionType {
        public val paramTypes: List<@WasmValueType Int>
        public val returnTypes: List<@WasmValueType Int>

        public companion object {
            public operator fun invoke(
                params: List<@WasmValueType Int>,
                returnTypes: List<@WasmValueType Int> = emptyList(),
            ): HostFunctionType = DefaultHostFunctionType(params, returnTypes)

            private data class DefaultHostFunctionType(
                override val paramTypes: List<@WasmValueType Int>,
                override val returnTypes: List<@WasmValueType Int>,
            ) : HostFunctionType
        }
    }
}
