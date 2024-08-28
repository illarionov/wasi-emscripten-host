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

    public const val O_RDONLY: UInt = 0x0U
    public const val O_WRONLY: UInt = 0x1U
    public const val O_RDWR: UInt = 0x2U
    public const val O_ACCMODE: UInt = 0x3U

    public const val O_CREAT: UInt = 0x40U
    public const val O_EXCL: UInt = 0x80U
    public const val O_NOCTTY: UInt = 0x100U
    public const val O_TRUNC: UInt = 0x200U
    public const val O_APPEND: UInt = 0x400U
    public const val O_NONBLOCK: UInt = 0x800U
    public const val O_NDELAY: UInt = O_NONBLOCK
    public const val O_DSYNC: UInt = 0x1000U
    public const val O_ASYNC: UInt = 0x2000U
    public const val O_DIRECT: UInt = 0x4000U
    public const val O_LARGEFILE: UInt = 0x8000U
    public const val O_DIRECTORY: UInt = 0x10000U
    public const val O_NOFOLLOW: UInt = 0x20000U
    public const val O_NOATIME: UInt = 0x40000U
    public const val O_CLOEXEC: UInt = 0x80000U
    public const val O_SYNC: UInt = 0x101000U
    public const val O_PATH: UInt = 0x200000U
    public const val O_TMPFILE: UInt = 0x410000U
    public const val O_SEARCH: UInt = O_PATH

    public const val AT_FDCWD: Int = -100
    public const val AT_SYMLINK_NOFOLLOW: UInt = 0x100U
    public const val AT_REMOVEDIR: UInt = 0x200U
    public const val AT_SYMLINK_FOLLOW: UInt = 0x400U
    public const val AT_EACCESS: UInt = 0x200U

    public const val F_OK: UInt = 0U
    public const val R_OK: UInt = 4U
    public const val W_OK: UInt = 2U
    public const val X_OK: UInt = 1U
}
