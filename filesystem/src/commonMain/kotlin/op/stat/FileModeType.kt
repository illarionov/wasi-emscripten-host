/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import at.released.weh.common.api.UintBitMask
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.fileModeTypeToString
import kotlin.jvm.JvmInline

/**
 * File mode and file type bits
 */
@JvmInline
public value class FileModeType(
    public override val mask: UInt,
) : UintBitMask<FileModeType> {
    override val newInstance: (UInt) -> FileModeType get() = ::FileModeType
    public val mode: FileMode
        get() = FileMode(mask and 0xfffU)

    public val type: Filetype
        get() = when (mask and S_IFMT) {
            S_IFDIR -> Filetype.DIRECTORY
            S_IFCHR -> Filetype.CHARACTER_DEVICE
            S_IFBLK -> Filetype.BLOCK_DEVICE
            S_IFREG -> Filetype.REGULAR_FILE
            S_IFIFO -> Filetype.UNKNOWN
            S_IFLNK -> Filetype.SYMBOLIC_LINK
            S_IFSOCK -> Filetype.SOCKET_STREAM // XXX
            else -> Filetype.UNKNOWN
        }

    override fun toString(): String = fileModeTypeToString(mask)

    // Constants from Emscripten include/sys/stat.h
    @Suppress("NoMultipleSpaces", "TOO_MANY_CONSECUTIVE_SPACES")
    public companion object {
        public const val S_IFMT:   UInt = 0b000_001_111_000_000_000_000U
        public const val S_IFDIR:  UInt = 0b000_000_100_000_000_000_000U
        public const val S_IFCHR:  UInt = 0b000_000_010_000_000_000_000U
        public const val S_IFBLK:  UInt = 0b000_000_110_000_000_000_000U
        public const val S_IFREG:  UInt = 0b000_001_000_000_000_000_000U
        public const val S_IFIFO:  UInt = 0b000_000_001_000_000_000_000U
        public const val S_IFLNK:  UInt = 0b000_001_010_000_000_000_000U
        public const val S_IFSOCK: UInt = 0b000_001_100_000_000_000_000U
    }
}
