/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import kotlinx.io.Buffer

@InternalWasiEmscriptenHostApi
public fun String.encodeToNullTerminatedBuffer(
    truncateAtSize: Int = Int.MAX_VALUE,
): Buffer = Buffer().also { buffer ->
    buffer.writeNullTerminatedString(this, truncateAtSize)
}

@InternalWasiEmscriptenHostApi
public fun String.encodedNullTerminatedStringLength(): Int = this.encodeToByteArray().size + 1
