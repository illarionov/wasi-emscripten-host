/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.stat

import arrow.core.Either
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.windows.fdresource.WindowsFileFdResource.WindowsFileChannel

internal fun windowsStatFd(
    channel: WindowsFileChannel
): Either<StatError, StructStat> {
    TODO()
//    return StructStat(
//        deviceId = st_dev.toLong(),
//        inode = st_ino.toLong(),
//        mode = posixModeTypeToFilemode(st_mode),
//        type = posixModeTypeToFiletype(st_mode),
//        links = st_nlink.toLong(),
//        usedId = 0L,
//        groupId = 0L,
//        specialFileDeviceId = st_rdev.toLong(),
//        size = st_size,
//        blockSize = st_blksize,
//        blocks = st_blocks,
//        accessTime = st_atim.toStructTimespec(),
//        modificationTime = st_mtim.toStructTimespec(),
//        changeStatusTime = st_ctim.toStructTimespec(),
//    )
}



