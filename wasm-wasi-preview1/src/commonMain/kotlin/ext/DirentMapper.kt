/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Dirent
import kotlinx.io.Sink
import kotlinx.io.writeIntLe
import kotlinx.io.writeLongLe

internal const val DIRENT_PACKED_SIZE = 24

internal fun Dirent.packTo(
    sink: Sink,
) {
    sink.writeLongLe(this.dNext)
    sink.writeLongLe(this.dIno)
    sink.writeIntLe(this.dNamlen)
    sink.writeIntLe(this.dType.code)
}
