/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Fdstat
import kotlinx.io.Sink
import kotlinx.io.writeIntLe
import kotlinx.io.writeLongLe

internal const val FDSTAT_PACKED_SIZE = 24

internal fun Fdstat.packTo(
    sink: Sink,
) {
    sink.writeIntLe(this.fsFiletype.code)
    sink.writeIntLe(this.fsFlags.toInt())
    sink.writeLongLe(this.fsRightsBase)
    sink.writeLongLe(this.fsRightsInheriting)
}
