/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("BLANK_LINE_BETWEEN_PROPERTIES")

package at.released.weh.emcripten.runtime.include

/**
 * Constants from Emscripten include/fcntl.h
 */
internal object Fcntl {
    const val F_DUPFD: UInt = 0U
    const val F_DUPFD_CLOEXEC: UInt = 1030U

    const val F_GETFD: UInt = 1U
    const val F_SETFD: UInt = 2U
    const val F_GETFL: UInt = 3U
    const val F_SETFL: UInt = 4U

    const val F_SETOWN: UInt = 8U
    const val F_GETOWN: UInt = 9U
    const val F_SETSIG: UInt = 10U
    const val F_GETSIG: UInt = 11U

    const val F_GETLK: UInt = 12U
    const val F_SETLK: UInt = 13U
    const val F_SETLKW: UInt = 14U

    const val F_SETOWN_EX: UInt = 15U
    const val F_GETOWN_EX: UInt = 16U
    const val F_GETOWNER_UIDS: UInt = 17U

    const val F_RDLCK: Short = 0
    const val F_WRLCK: Short = 1
    const val F_UNLCK: Short = 2

    const val O_RDONLY: Int = 0x0
    const val O_WRONLY: Int = 0x1
    const val O_RDWR: Int = 0x2
    const val O_ACCMODE: Int = 0x3

    const val O_CREAT: Int = 0x40
    const val O_EXCL: Int = 0x80
    const val O_NOCTTY: Int = 0x100
    const val O_TRUNC: Int = 0x200
    const val O_APPEND: Int = 0x400
    const val O_NONBLOCK: Int = 0x800
    const val O_NDELAY: Int = O_NONBLOCK
    const val O_DSYNC: Int = 0x1000
    const val O_ASYNC: Int = 0x2000
    const val O_DIRECT: Int = 0x4000
    const val O_LARGEFILE: Int = 0x8000
    const val O_DIRECTORY: Int = 0x10000
    const val O_NOFOLLOW: Int = 0x20000
    const val O_NOATIME: Int = 0x40000
    const val O_CLOEXEC: Int = 0x80000
    const val O_SYNC: Int = 0x101000
    const val O_PATH: Int = 0x200000
    const val O_TMPFILE: Int = 0x410000
    const val O_SEARCH: Int = O_PATH

    const val AT_FDCWD: Int = -100
    const val AT_SYMLINK_NOFOLLOW: Int = 0x100
    const val AT_REMOVEDIR: Int = 0x200
    const val AT_SYMLINK_FOLLOW: Int = 0x400
    const val AT_EACCESS: Int = 0x200

    const val F_OK: Int = 0
    const val R_OK: Int = 4
    const val W_OK: Int = 2
    const val X_OK: Int = 1
}
