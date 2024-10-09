/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Prestat
import at.released.weh.wasi.preview1.type.PrestatDir
import kotlinx.io.Sink
import kotlinx.io.writeIntLe

internal const val PRESTAT_PACKED_SIZE = 8

internal fun Prestat.packTo(
    sink: Sink,
): Unit = when (this) {
    is PrestatDir -> this.packTo(sink)
}

internal fun PrestatDir.packTo(
    sink: Sink,
): Unit = sink.run {
    writeIntLe(0) // preopentype: prestat_dir
    writeIntLe(this@packTo.prNameLen)
}
