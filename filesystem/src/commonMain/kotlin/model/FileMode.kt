/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

import androidx.annotation.IntDef
import at.released.weh.filesystem.model.FileModeFlag.S_IRGRP
import at.released.weh.filesystem.model.FileModeFlag.S_IROTH
import at.released.weh.filesystem.model.FileModeFlag.S_IRUSR
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXG
import at.released.weh.filesystem.model.FileModeFlag.S_IRWXU
import at.released.weh.filesystem.model.FileModeFlag.S_ISGID
import at.released.weh.filesystem.model.FileModeFlag.S_ISUID
import at.released.weh.filesystem.model.FileModeFlag.S_ISVTX
import at.released.weh.filesystem.model.FileModeFlag.S_IWGRP
import at.released.weh.filesystem.model.FileModeFlag.S_IWOTH
import at.released.weh.filesystem.model.FileModeFlag.S_IWUSR
import at.released.weh.filesystem.model.FileModeFlag.S_IXGRP
import at.released.weh.filesystem.model.FileModeFlag.S_IXOTH
import at.released.weh.filesystem.model.FileModeFlag.S_IXUSR
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * File mode bits (mode_t)
 */
@Retention(SOURCE)
@IntDef(
    flag = true,
    value = [
        S_ISUID,
        S_ISGID,
        S_ISVTX,
        S_IRUSR,
        S_IWUSR,
        S_IXUSR,
        S_IRWXU,
        S_IRGRP,
        S_IWGRP,
        S_IXGRP,
        S_IRWXG,
        S_IROTH,
        S_IWOTH,
        S_IXOTH,
    ],
)
public annotation class FileMode

public object FileModeFlag {
    public const val S_ISUID: Int = 0b100_000_000_000
    public const val S_ISGID: Int = 0b010_000_000_000
    public const val S_ISVTX: Int = 0b001_000_000_000
    public const val S_IRUSR: Int = 0b000_100_000_000
    public const val S_IWUSR: Int = 0b000_010_000_000
    public const val S_IXUSR: Int = 0b000_001_000_000
    public const val S_IRWXU: Int = 0b000_111_000_000
    public const val S_IRGRP: Int = 0b000_000_100_000
    public const val S_IWGRP: Int = 0b000_000_010_000
    public const val S_IXGRP: Int = 0b000_000_001_000
    public const val S_IRWXG: Int = 0b000_000_111_000
    public const val S_IROTH: Int = 0b000_000_000_100
    public const val S_IWOTH: Int = 0b000_000_000_010
    public const val S_IXOTH: Int = 0b000_000_000_001
    public const val S_IRWXO: Int = 0b000_000_000_111
}
