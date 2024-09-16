/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("BLANK_LINE_BETWEEN_PROPERTIES")

package at.released.weh.host.include

/**
 * Constants from Emscripten include/fcntl.h
 */
public object Fcntl {
    public const val F_DUPFD: UInt = 0U
    public const val F_DUPFD_CLOEXEC: UInt = 1030U

    public const val F_GETFD: UInt = 1U
    public const val F_SETFD: UInt = 2U
    public const val F_GETFL: UInt = 3U
    public const val F_SETFL: UInt = 4U

    public const val F_SETOWN: UInt = 8U
    public const val F_GETOWN: UInt = 9U
    public const val F_SETSIG: UInt = 10U
    public const val F_GETSIG: UInt = 11U

    public const val F_GETLK: UInt = 12U
    public const val F_SETLK: UInt = 13U
    public const val F_SETLKW: UInt = 14U

    public const val F_SETOWN_EX: UInt = 15U
    public const val F_GETOWN_EX: UInt = 16U
    public const val F_GETOWNER_UIDS: UInt = 17U

    public const val F_RDLCK: Short = 0
    public const val F_WRLCK: Short = 1
    public const val F_UNLCK: Short = 2

    public const val O_RDONLY: Int = 0x0
    public const val O_WRONLY: Int = 0x1
    public const val O_RDWR: Int = 0x2
    public const val O_ACCMODE: Int = 0x3

    public const val O_CREAT: Int = 0x40
    public const val O_EXCL: Int = 0x80
    public const val O_NOCTTY: Int = 0x100
    public const val O_TRUNC: Int = 0x200
    public const val O_APPEND: Int = 0x400
    public const val O_NONBLOCK: Int = 0x800
    public const val O_NDELAY: Int = O_NONBLOCK
    public const val O_DSYNC: Int = 0x1000
    public const val O_ASYNC: Int = 0x2000
    public const val O_DIRECT: Int = 0x4000
    public const val O_LARGEFILE: Int = 0x8000
    public const val O_DIRECTORY: Int = 0x10000
    public const val O_NOFOLLOW: Int = 0x20000
    public const val O_NOATIME: Int = 0x40000
    public const val O_CLOEXEC: Int = 0x80000
    public const val O_SYNC: Int = 0x101000
    public const val O_PATH: Int = 0x200000
    public const val O_TMPFILE: Int = 0x410000
    public const val O_SEARCH: Int = O_PATH

    public const val AT_FDCWD: Int = -100
    public const val AT_SYMLINK_NOFOLLOW: Int = 0x100
    public const val AT_REMOVEDIR: Int = 0x200
    public const val AT_SYMLINK_FOLLOW: Int = 0x400
    public const val AT_EACCESS: Int = 0x200

    public const val F_OK: Int = 0
    public const val R_OK: Int = 4
    public const val W_OK: Int = 2
    public const val X_OK: Int = 1
}
