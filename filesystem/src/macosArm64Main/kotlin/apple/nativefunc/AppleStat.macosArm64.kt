/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.nativefunc

import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.posix.ext.posixModeTypeToFilemode
import at.released.weh.filesystem.posix.ext.posixModeTypeToFiletype
import platform.posix.stat

internal actual fun stat.toStructStat(): StructStat = StructStat(
    deviceId = st_dev.toLong(),
    inode = st_ino.toLong(),
    mode = posixModeTypeToFilemode(st_mode.toUInt()),
    type = posixModeTypeToFiletype(st_mode.toUInt()),
    links = st_nlink.toLong(),
    usedId = st_uid.toLong(),
    groupId = st_gid.toLong(),
    specialFileDeviceId = st_rdev.toLong(),
    size = st_size,
    blockSize = st_blksize.toLong(),
    blocks = st_blocks,
    accessTime = st_atimespec.toStructTimespec(),
    modificationTime = st_mtimespec.toStructTimespec(),
    changeStatusTime = st_ctimespec.toStructTimespec(),
)
