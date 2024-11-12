/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.readdir

import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.readdir.DirEntry
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.DIR
import platform.posix.DT_BLK
import platform.posix.DT_CHR
import platform.posix.DT_DIR
import platform.posix.DT_FIFO
import platform.posix.DT_LNK
import platform.posix.DT_REG
import platform.posix.DT_SOCK
import platform.posix.DT_UNKNOWN
import platform.posix.EBADF
import platform.posix.dirent
import platform.posix.errno
import platform.posix.readdir
import platform.posix.set_posix_errno
import platform.posix.strerror

internal expect val dirent.inode: Long
internal expect val dirent.cookie: Long

internal fun posixReadDir(
    dir: CPointer<DIR>,
): PosixReadDirResult {
    set_posix_errno(0)
    val dirent: CPointer<dirent>? = readdir(dir)
    if (dirent == null) {
        val error = errno
        return if (error != 0) {
            PosixReadDirResult.Error(error.errnoToReadDirError())
        } else {
            PosixReadDirResult.EndOfStream
        }
    }

    val srcType = dirent[0].d_type.toInt()
    val srcIno = dirent[0].inode

    val (fileType, inode) = if (srcType == DT_UNKNOWN || srcIno == 0L) {
        // XXX read using lstat?
        Filetype.UNKNOWN to 0L
    } else {
        dTypeToFiletype(srcType) to srcIno
    }

    return PosixReadDirResult.Entry(
        DirEntry(
            name = dirent[0].d_name.toKStringFromUtf8(),
            type = fileType,
            inode = inode,
            cookie = dirent[0].cookie,
        ),
    )
}

private fun Int.errnoToReadDirError(): ReadDirError {
    return when (this) {
        EBADF -> BadFileDescriptor("Not a directory stream descriptor")
        else -> IoError("Error `$this` (${strerror(this)?.toKStringFromUtf8()})")
    }
}

private fun dTypeToFiletype(dType: Int): Filetype = when (dType) {
    DT_BLK -> Filetype.BLOCK_DEVICE
    DT_CHR -> Filetype.CHARACTER_DEVICE
    DT_DIR -> Filetype.DIRECTORY
    DT_FIFO -> Filetype.UNKNOWN
    DT_LNK -> Filetype.SYMBOLIC_LINK
    DT_REG -> Filetype.REGULAR_FILE
    DT_SOCK -> Filetype.SOCKET_STREAM
    else -> Filetype.UNKNOWN
}
