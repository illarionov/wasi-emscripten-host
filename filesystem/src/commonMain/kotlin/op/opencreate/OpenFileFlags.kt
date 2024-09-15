/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.opencreate

import androidx.annotation.IntDef
import at.released.weh.common.ext.maskToString
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_ACCMODE
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_APPEND
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_ASYNC
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_CLOEXEC
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_CREAT
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECT
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DSYNC
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_EXCL
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_LARGEFILE
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NDELAY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOATIME
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOCTTY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOFOLLOW
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NONBLOCK
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_PATH
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_RDONLY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_RDWR
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_SEARCH
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_SYNC
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_TMPFILE
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_TRUNC
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_WRONLY
import kotlin.annotation.AnnotationRetention.SOURCE

@Retention(SOURCE)
@IntDef(
    flag = true,
    value = [
        O_RDONLY,
        O_WRONLY,
        O_RDWR,
        O_ACCMODE,
        O_CREAT,
        O_EXCL,
        O_NOCTTY,
        O_TRUNC,
        O_APPEND,
        O_NONBLOCK,
        O_NDELAY,
        O_DSYNC,
        O_ASYNC,
        O_DIRECT,
        O_LARGEFILE,
        O_DIRECTORY,
        O_NOFOLLOW,
        O_NOATIME,
        O_CLOEXEC,
        O_SYNC,
        O_PATH,
        O_TMPFILE,
        O_SEARCH,
    ],
)
public annotation class OpenFileFlags

@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
public object OpenFileFlag {
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

    internal fun openFileFlagsToString(
        @OpenFileFlags mask: Int,
    ): String {
        return "OpenFileFlags(0x${mask.toString(16)}: ${openFileFlagsToStringVerbose(mask)})"
    }

    internal fun openFileFlagsToStringVerbose(
        @OpenFileFlags mask: Int,
    ): String {
        val startNames = if ((mask and O_ACCMODE) == 0) {
            listOf(::O_RDONLY.name)
        } else {
            emptyList()
        }
        return maskToString(
            mask,
            listOf(
                ::O_WRONLY,
                ::O_RDWR,
                ::O_CREAT,
                ::O_EXCL,
                ::O_NOCTTY,
                ::O_TRUNC,
                ::O_APPEND,
                ::O_NONBLOCK,
                ::O_SYNC,
                ::O_TMPFILE,
                ::O_DSYNC,
                ::O_ASYNC,
                ::O_DIRECT,
                ::O_LARGEFILE,
                ::O_DIRECTORY,
                ::O_NOFOLLOW,
                ::O_NOATIME,
                ::O_CLOEXEC,
                ::O_PATH,
            ),
            startNames,
        )
    }
}
