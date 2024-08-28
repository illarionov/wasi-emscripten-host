/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber", "ConstructorParameterNaming", "TYPEALIAS_NAME_INCORRECT_CASE")

package at.released.weh.host.include.sys

import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.op.stat.timeMillis
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.writeIntLe
import kotlinx.io.writeLongLe

public fun StructStat.packTo(sink: Sink): Unit = sink.run {
    writeIntLe(deviceId.toInt()) // 0
    writeIntLe(mode.mask.toInt()) // 4
    writeIntLe(links.toInt()) // 8
    writeIntLe(usedId.toInt()) // 12
    writeIntLe(groupId.toInt()) // 16
    writeIntLe(specialFileDeviceId.toInt()) // 20
    writeLongLe(size.toLong()) // 24
    writeIntLe(4096) // 32
    writeIntLe(blocks.toInt()) // 36

    accessTime.timeMillis.let {
        writeLongLe((it / 1000U).toLong()) // 40
        writeIntLe((1000U * (it % 1000U)).toInt()) // 48
        writeIntLe(0) // 52, padding
    }
    modificationTime.timeMillis.let {
        writeLongLe((it / 1000U).toLong()) // 56
        writeIntLe((1000U * (it % 1000U)).toInt()) // 64
        writeIntLe(0) // 68, padding
    }
    changeStatusTime.timeMillis.let {
        writeLongLe((it / 1000U).toLong()) // 72
        writeIntLe((1000U * (it % 1000U)).toInt()) // 80
        writeIntLe(0) // 84, padding
    }
    writeLongLe(inode.toLong()) // 88
}

public const val STRUCT_SIZE_PACKED_SIZE: Int = 96

public fun StructStat.pack(): Buffer = Buffer().also {
    packTo(it)
    check(it.size == STRUCT_SIZE_PACKED_SIZE.toLong())
}
