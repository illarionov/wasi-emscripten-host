/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.parser.model

import at.released.weh.gradle.wasm.codegen.witx.parser.model.WasiNumberType.UnsignedNumber

internal sealed interface WasiType {
    object Handle : WasiType {
        override fun toString(): String = "Handle"
    }

    data class NumberType(
        val type: WasiNumberType,
    ) : WasiType

    data class EnumType(
        val tag: UnsignedNumber,
        val values: List<IdentifierWithComment>,
    ) : WasiType

    data class FlagsType(
        val repr: UnsignedNumber,
        val flags: List<IdentifierWithComment>,
    ) : WasiType

    data class RecordType(
        val fields: List<RecordField>,
    ) : WasiType {
        data class RecordField(
            val identifier: Identifier,
            val type: RecordFieldType,
            val comment: String,
        )

        sealed class RecordFieldType {
            data class IdentifierField(val identifier: Identifier) : RecordFieldType()
            data class Pointer(
                val dstType: UnsignedNumber,
                val isConstPointer: Boolean,
            ) : RecordFieldType()
        }
    }

    data class ListType(val identifier: Identifier) : WasiType

    data class UnionType(
        val tag: Identifier,
        val members: List<Identifier>,
    ) : WasiType
}
