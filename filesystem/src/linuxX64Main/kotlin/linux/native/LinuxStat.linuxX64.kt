/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.native

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.linux.ext.toStructTimespec
import at.released.weh.filesystem.linux.fileModeFromLinuxModeType
import at.released.weh.filesystem.linux.fileTypeFromLinuxModeType
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.platform.linux.fstatat
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.errno
import platform.posix.fstat
import platform.posix.stat

internal actual fun platformFstatat(
    dirfd: Int,
    path: String,
    statFlags: Int,
): Either<Int, StructStat> = memScoped {
    val statBuf: stat = alloc()
    val exitCode = fstatat(
        dirfd,
        path,
        statBuf.ptr,
        statFlags,
    )
    return if (exitCode == 0) {
        statBuf.toStructStat().right()
    } else {
        errno.left()
    }
}

internal actual fun platformFstatFd(fd: Int): Either<Int, StructStat> = memScoped {
    val statBuf: stat = alloc()
    val exitCode = fstat(fd, statBuf.ptr)
    return if (exitCode == 0) {
        statBuf.toStructStat().right()
    } else {
        errno.left()
    }
}

internal fun stat.toStructStat(): StructStat = StructStat(
    deviceId = st_dev.toLong(),
    inode = st_ino.toLong(),
    mode = fileModeFromLinuxModeType(st_mode),
    type = fileTypeFromLinuxModeType(st_mode),
    links = st_nlink.toLong(),
    usedId = st_uid.toLong(),
    groupId = st_gid.toLong(),
    specialFileDeviceId = st_rdev.toLong(),
    size = st_size,
    blockSize = st_blksize,
    blocks = st_blocks,
    accessTime = st_atim.toStructTimespec(),
    modificationTime = st_mtim.toStructTimespec(),
    changeStatusTime = st_ctim.toStructTimespec(),
)
