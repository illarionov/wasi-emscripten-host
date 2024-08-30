/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

import at.released.weh.common.api.UintBitMask
import kotlin.jvm.JvmInline

/**
 * File mode bits (mode_t)
 */
@JvmInline
public value class FileMode(
    public override val mask: UInt,
) : UintBitMask<FileMode> {
    override val newInstance: (UInt) -> FileMode get() = ::FileMode
    override fun toString(): String = fileModeTypeToString(mask)

    // Constants from Emscripten include/sys/stat.h
    public companion object {
        public const val S_ISUID: UInt = 0b100_000_000_000U
        public const val S_ISGID: UInt = 0b010_000_000_000U
        public const val S_ISVTX: UInt = 0b001_000_000_000U
        public const val S_IRUSR: UInt = 0b000_100_000_000U
        public const val S_IWUSR: UInt = 0b000_010_000_000U
        public const val S_IXUSR: UInt = 0b000_001_000_000U
        public const val S_IRWXU: UInt = 0b000_111_000_000U
        public const val S_IRGRP: UInt = 0b000_000_100_000U
        public const val S_IWGRP: UInt = 0b000_000_010_000U
        public const val S_IXGRP: UInt = 0b000_000_001_000U
        public const val S_IRWXG: UInt = 0b000_000_111_000U
        public const val S_IROTH: UInt = 0b000_000_000_100U
        public const val S_IWOTH: UInt = 0b000_000_000_010U
        public const val S_IXOTH: UInt = 0b000_000_000_001U
        public const val S_IRWXO: UInt = 0b000_000_000_111U
    }
}

internal fun fileModeTypeToString(mask: UInt): String = "0${mask.toString(8)}"
