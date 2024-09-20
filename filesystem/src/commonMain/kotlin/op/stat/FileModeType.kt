/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import androidx.annotation.IntDef
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.FileModeFlag
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.jvm.JvmStatic

/**
 * File mode and file type bits
 */
@Retention(SOURCE)
@IntDef(
    flag = true,
    value = [
        FileTypeFlag.S_IFMT,
        FileTypeFlag.S_IFDIR,
        FileTypeFlag.S_IFCHR,
        FileTypeFlag.S_IFBLK,
        FileTypeFlag.S_IFREG,
        FileTypeFlag.S_IFIFO,
        FileTypeFlag.S_IFLNK,
        FileTypeFlag.S_IFSOCK,
        FileModeFlag.S_ISUID,
        FileModeFlag.S_ISGID,
        FileModeFlag.S_ISVTX,
        FileModeFlag.S_IRUSR,
        FileModeFlag.S_IWUSR,
        FileModeFlag.S_IXUSR,
        FileModeFlag.S_IRWXU,
        FileModeFlag.S_IRGRP,
        FileModeFlag.S_IWGRP,
        FileModeFlag.S_IXGRP,
        FileModeFlag.S_IRWXG,
        FileModeFlag.S_IROTH,
        FileModeFlag.S_IWOTH,
        FileModeFlag.S_IXOTH,
    ],
)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
)
public annotation class FileModeType

// Constants from Emscripten include/sys/stat.h
@Suppress("NoMultipleSpaces", "TOO_MANY_CONSECUTIVE_SPACES")
public object FileTypeFlag {
    public const val S_IFMT:   Int = 0b000_001_111_000_000_000_000
    public const val S_IFDIR:  Int = 0b000_000_100_000_000_000_000
    public const val S_IFCHR:  Int = 0b000_000_010_000_000_000_000
    public const val S_IFBLK:  Int = 0b000_000_110_000_000_000_000
    public const val S_IFREG:  Int = 0b000_001_000_000_000_000_000
    public const val S_IFIFO:  Int = 0b000_000_001_000_000_000_000
    public const val S_IFLNK:  Int = 0b000_001_010_000_000_000_000
    public const val S_IFSOCK: Int = 0b000_001_100_000_000_000_000

    @JvmStatic
    internal fun fileModeTypeToString(
        @FileModeType mask: Int,
    ): String = "0${mask.toString(8)}"

    @JvmStatic
    @FileMode
    @Suppress("MagicNumber")
    internal fun fileModeTypeToFileMode(
        @FileModeType mask: Int,
    ): Int = mask and 0xfff

    @JvmStatic
    internal fun fileModeTypeToFileType(
        @FileModeType mask: Int,
    ): Filetype = when (mask and S_IFMT) {
        S_IFDIR -> Filetype.DIRECTORY
        S_IFCHR -> Filetype.CHARACTER_DEVICE
        S_IFBLK -> Filetype.BLOCK_DEVICE
        S_IFREG -> Filetype.REGULAR_FILE
        S_IFIFO -> Filetype.UNKNOWN
        S_IFLNK -> Filetype.SYMBOLIC_LINK
        S_IFSOCK -> Filetype.SOCKET_STREAM // XXX
        else -> Filetype.UNKNOWN
    }
}
