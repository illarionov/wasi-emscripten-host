/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.include.sys

import androidx.annotation.IntDef
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * Mmap flags
 *
 * <sys/mman.h>
 */
@IntDef(
    flag = true,
    value = [
        MmapFlag.MAP_HUGE_MASK,
        MmapFlag.MAP_SHARED,
        MmapFlag.MAP_PRIVATE,
        MmapFlag.MAP_SHARED_VALIDATE,
        MmapFlag.MAP_TYPE,
        MmapFlag.MAP_FIXED,
        MmapFlag.MAP_ANON,
        MmapFlag.MAP_ANONYMOUS,
        MmapFlag.MAP_NORESERVE,
        MmapFlag.MAP_GROWSDOWN,
        MmapFlag.MAP_DENYWRITE,
        MmapFlag.MAP_EXECUTABLE,
        MmapFlag.MAP_LOCKED,
        MmapFlag.MAP_POPULATE,
        MmapFlag.MAP_NONBLOCK,
        MmapFlag.MAP_STACK,
        MmapFlag.MAP_HUGETLB,
        MmapFlag.MAP_SYNC,
        MmapFlag.MAP_FIXED_NOREPLACE,
        MmapFlag.MAP_FILE,
        MmapFlag.MAP_HUGE_16KB,
        MmapFlag.MAP_HUGE_64KB,
        MmapFlag.MAP_HUGE_512KB,
        MmapFlag.MAP_HUGE_1MB,
        MmapFlag.MAP_HUGE_2MB,
        MmapFlag.MAP_HUGE_8MB,
        MmapFlag.MAP_HUGE_16MB,
        MmapFlag.MAP_HUGE_32MB,
        MmapFlag.MAP_HUGE_256MB,
        MmapFlag.MAP_HUGE_512MB,
        MmapFlag.MAP_HUGE_1GB,
        MmapFlag.MAP_HUGE_2GB,
        MmapFlag.MAP_HUGE_16GB,
    ],
)
@Retention(SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.VALUE_PARAMETER,
)
internal annotation class SysMmanMapFlags

@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
internal object MmapFlag {
    const val MAP_HUGE_SHIFT: Int = 26
    const val MAP_HUGE_MASK: Int = 0x3f

    const val MAP_SHARED: Int = 0x01
    const val MAP_PRIVATE: Int = 0x02
    const val MAP_SHARED_VALIDATE: Int = 0x03
    const val MAP_TYPE: Int = 0x0f
    const val MAP_FIXED: Int = 0x10
    const val MAP_ANON: Int = 0x20
    const val MAP_ANONYMOUS: Int = MAP_ANON
    const val MAP_NORESERVE: Int = 0x4000
    const val MAP_GROWSDOWN: Int = 0x0100
    const val MAP_DENYWRITE: Int = 0x0800
    const val MAP_EXECUTABLE: Int = 0x1000
    const val MAP_LOCKED: Int = 0x2000
    const val MAP_POPULATE: Int = 0x8000
    const val MAP_NONBLOCK: Int = 0x10000
    const val MAP_STACK: Int = 0x20000
    const val MAP_HUGETLB: Int = 0x40000
    const val MAP_SYNC: Int = 0x80000
    const val MAP_FIXED_NOREPLACE: Int = 0x100000
    const val MAP_FILE: Int = 0

    const val MAP_HUGE_16KB: Int = 14.shl(26)
    const val MAP_HUGE_64KB: Int = 16.shl(26)
    const val MAP_HUGE_512KB: Int = 19.shl(26)
    const val MAP_HUGE_1MB: Int = 20.shl(26)
    const val MAP_HUGE_2MB: Int = 21.shl(26)
    const val MAP_HUGE_8MB: Int = 23.shl(26)
    const val MAP_HUGE_16MB: Int = 24.shl(26)
    const val MAP_HUGE_32MB: Int = 25.shl(26)
    const val MAP_HUGE_256MB: Int = 28.shl(26)
    const val MAP_HUGE_512MB: Int = 29.shl(26)
    const val MAP_HUGE_1GB: Int = 30.shl(26)
    const val MAP_HUGE_2GB: Int = 31.shl(26)
    const val MAP_HUGE_16GB: Int = 34.shl(26)
}
