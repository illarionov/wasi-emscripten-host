/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.parser.model

internal data class WasiFunc(
    val export: String,
    val params: List<WasiFuncParam>,
    val result: WasiFuncResult?,
    val comment: String,
) {
    internal data class WasiFuncParam(
        val name: Identifier,
        val type: ParamType,
        val comment: String,
    ) {
        internal sealed class ParamType {
            object String : ParamType()
            data class NumberType(val type: WasiNumberType) : ParamType()
            data class WasiType(val identifier: Identifier) : ParamType()
            data class Pointer(
                val dst: ParamType,
                val isConstPointer: Boolean,
            ) : ParamType()
        }
    }

    internal data class WasiFuncResult(
        val identifier: Identifier,
        val expectedData: ExpectedData?,
        val expectedError: Identifier,
        val comment: String,
    ) {
        internal sealed class ExpectedData {
            data class WasiType(val identifier: Identifier) : ExpectedData()
            data class Tuple(
                val first: Identifier,
                val second: Identifier,
            ) : ExpectedData()
        }
    }
}
