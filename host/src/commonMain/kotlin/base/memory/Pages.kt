/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

public const val WASM_MEMORY_PAGE_SIZE: Long = 65_536L
public val WASM_MEMORY_DEFAULT_MAX_PAGES: Pages = Pages(32_768L)
public val WASM_MEMORY_32_MAX_PAGES: Pages = Pages(65_536L)
public val WASM_MEMORY_64_MAX_PAGES: Pages = Pages(281_474_976_710_656)

public class Pages(
    public val count: Long,
) {
    public val inBytes: Long
        get() = count * WASM_MEMORY_PAGE_SIZE

    init {
        require(count in 0..WASM_MEMORY_64_MAX_PAGES.count)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as Pages

        return count == other.count
    }

    override fun hashCode(): Int {
        return count.hashCode()
    }

    override fun toString(): String {
        return "Pages($count)"
    }
}
