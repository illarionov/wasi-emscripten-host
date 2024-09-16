/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.function

public class IndirectFunctionTableIndex(
    public val funcId: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as IndirectFunctionTableIndex

        return funcId == other.funcId
    }

    override fun hashCode(): Int {
        return funcId
    }

    override fun toString(): String {
        return "IndirectFunctionTableIndex(funcId=$funcId)"
    }
}
