/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasm.core

/**
 * Interface defining a WebAssembly function implemented on the host
 */
public interface HostFunction {
    public val wasmName: String
    public val type: HostFunctionType

    public interface HostFunctionType {
        @WasmValueType
        public val paramTypes: List<Int>

        @WasmValueType
        public val returnTypes: List<Int>

        public companion object {
            public operator fun invoke(
                @WasmValueType
                params: List<Int>,

                @WasmValueType
                returnTypes: List<Int> = emptyList(),
            ): HostFunctionType = DefaultHostFunctionType(params, returnTypes)

            private data class DefaultHostFunctionType(
                @WasmValueType
                override val paramTypes: List<Int>,

                @WasmValueType
                override val returnTypes: List<Int>,
            ) : HostFunctionType
        }
    }
}
