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
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.posix.ext.posixModeTypeToFilemode
import at.released.weh.filesystem.posix.ext.posixModeTypeToFiletype
import at.released.weh.host.platform.linux.fstatat
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.errno
import platform.posix.fstat
import platform.posix.stat

internal actual fun platformFstatat(
    dirfd: Int,
    path: PosixRealPath,
    statFlags: Int,
): Either<Int, StructStat> = memScoped {
    val statBuf: stat = alloc()
    val exitCode = fstatat(
        dirfd,
        path.kString,
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
    mode = posixModeTypeToFilemode(st_mode),
    type = posixModeTypeToFiletype(st_mode),
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
