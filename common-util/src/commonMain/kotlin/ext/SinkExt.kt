/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.common.ext

import kotlinx.io.Sink

public fun Sink.writeNullTerminatedString(
    string: String,
    truncateAtSize: Int = Int.MAX_VALUE,
): Int {
    require(truncateAtSize > 0)
    val raw = string.encodeToByteArray()
    val rawSize = raw.size.coerceAtMost(truncateAtSize - 1)
    write(raw, 0, rawSize)
    writeByte(0)
    return rawSize + 1
}
