/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.include.sys

import at.released.weh.filesystem.model.Filetype

// Constants from Emscripten include/sys/stat.h
@Suppress("NoMultipleSpaces", "TOO_MANY_CONSECUTIVE_SPACES")
internal object FileTypeFlag {
    public const val S_IFMT:   Int = 0b000_001_111_000_000_000_000
    public const val S_IFDIR:  Int = 0b000_000_100_000_000_000_000
    public const val S_IFCHR:  Int = 0b000_000_010_000_000_000_000
    public const val S_IFBLK:  Int = 0b000_000_110_000_000_000_000
    public const val S_IFREG:  Int = 0b000_001_000_000_000_000_000
    public const val S_IFIFO:  Int = 0b000_000_001_000_000_000_000
    public const val S_IFLNK:  Int = 0b000_001_010_000_000_000_000
    public const val S_IFSOCK: Int = 0b000_001_100_000_000_000_000

    internal fun fileTypeToFileMode(
        type: Filetype,
    ): Int = when (type) {
        Filetype.UNKNOWN -> 0
        Filetype.BLOCK_DEVICE -> S_IFBLK
        Filetype.CHARACTER_DEVICE -> S_IFCHR
        Filetype.DIRECTORY -> S_IFDIR
        Filetype.REGULAR_FILE -> S_IFREG
        Filetype.SOCKET_DGRAM -> S_IFSOCK
        Filetype.SOCKET_STREAM -> S_IFSOCK
        Filetype.SYMBOLIC_LINK -> S_IFLNK
    }
}
