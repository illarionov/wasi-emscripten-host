/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.timeNanos
import at.released.weh.wasi.preview1.type.Filestat
import at.released.weh.wasi.preview1.type.Filetype
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

internal fun StructStat.toFilestat(): Filestat = Filestat(
    dev = this.deviceId,
    ino = this.inode,
    filetype = checkNotNull(Filetype.fromCode(this.type.id)) {
        "Unexpected type ${this.type.id}"
    },
    nlink = this.links,
    size = this.size,
    atim = this.accessTime.timeNanos,
    mtim = this.modificationTime.timeNanos,
    ctim = this.changeStatusTime.timeNanos,
)
