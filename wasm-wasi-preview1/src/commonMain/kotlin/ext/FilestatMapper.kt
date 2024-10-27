/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Filestat
import kotlinx.io.Sink
import kotlinx.io.writeIntLe
import kotlinx.io.writeLongLe

internal const val FILESTAT_PACKED_SIZE = 60

internal fun Filestat.packTo(
    sink: Sink,
) {
    sink.writeLongLe(this.dev)
    sink.writeLongLe(this.ino)
    sink.writeIntLe(this.filetype.code)
    sink.writeLongLe(this.nlink)
    sink.writeLongLe(this.size)
    sink.writeLongLe(this.atim)
    sink.writeLongLe(this.mtim)
    sink.writeLongLe(this.ctim)
}
