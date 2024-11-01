/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Fdstat
import kotlinx.io.Sink
import kotlinx.io.writeLongLe
import kotlinx.io.writeShortLe

internal const val FDSTAT_PACKED_SIZE = 24

internal fun Fdstat.packTo(
    sink: Sink,
) {
    sink.writeByte(this.fsFiletype.code.toByte())
    sink.writeByte(0) // Alignment
    sink.writeShortLe(this.fsFlags)
    sink.writeShortLe(0) // Alignment
    sink.writeShortLe(0) // Alignment
    sink.writeLongLe(this.fsRightsBase)
    sink.writeLongLe(this.fsRightsInheriting)
}
