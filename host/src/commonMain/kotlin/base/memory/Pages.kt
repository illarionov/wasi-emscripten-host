/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base.memory

import kotlin.jvm.JvmInline

public const val WASM_MEMORY_PAGE_SIZE: Long = 65_536L
public val WASM_MEMORY_SQLITE_MAX_PAGES: Pages = Pages(32_768L)
public val WASM_MEMORY_32_MAX_PAGES: Pages = Pages(65_536L)
public val WASM_MEMORY_64_MAX_PAGES: Pages = Pages(281_474_976_710_656)

@JvmInline
public value class Pages(
    public val count: Long,
) {
    public val inBytes: Long
        get() = count * WASM_MEMORY_PAGE_SIZE
}
