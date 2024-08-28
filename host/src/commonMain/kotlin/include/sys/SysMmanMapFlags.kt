/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.include.sys

import at.released.weh.common.api.SqliteUintBitMask
import kotlin.jvm.JvmInline

/**
 * Mmap flags
 *
 * <sys/mman.h>
 */
@JvmInline
public value class SysMmanMapFlags(
    public override val mask: UInt,
) : SqliteUintBitMask<SysMmanProt> {
    override val newInstance: (UInt) -> SysMmanProt get() = ::SysMmanProt

    @Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
    public companion object {
        public const val MAP_HUGE_SHIFT: UInt = 26U
        public const val MAP_HUGE_MASK: UInt = 0x3fU

        public val MAP_SHARED: SysMmanMapFlags = SysMmanMapFlags(0x01U)
        public val MAP_PRIVATE: SysMmanMapFlags = SysMmanMapFlags(0x02U)
        public val MAP_SHARED_VALIDATE: SysMmanMapFlags = SysMmanMapFlags(0x03U)
        public val MAP_TYPE: SysMmanMapFlags = SysMmanMapFlags(0x0fU)
        public val MAP_FIXED: SysMmanMapFlags = SysMmanMapFlags(0x10U)
        public val MAP_ANON: SysMmanMapFlags = SysMmanMapFlags(0x20U)
        public val MAP_ANONYMOUS: SysMmanMapFlags = MAP_ANON
        public val MAP_NORESERVE: SysMmanMapFlags = SysMmanMapFlags(0x4000U)
        public val MAP_GROWSDOWN: SysMmanMapFlags = SysMmanMapFlags(0x0100U)
        public val MAP_DENYWRITE: SysMmanMapFlags = SysMmanMapFlags(0x0800U)
        public val MAP_EXECUTABLE: SysMmanMapFlags = SysMmanMapFlags(0x1000U)
        public val MAP_LOCKED: SysMmanMapFlags = SysMmanMapFlags(0x2000U)
        public val MAP_POPULATE: SysMmanMapFlags = SysMmanMapFlags(0x8000U)
        public val MAP_NONBLOCK: SysMmanMapFlags = SysMmanMapFlags(0x10000U)
        public val MAP_STACK: SysMmanMapFlags = SysMmanMapFlags(0x20000U)
        public val MAP_HUGETLB: SysMmanMapFlags = SysMmanMapFlags(0x40000U)
        public val MAP_SYNC: SysMmanMapFlags = SysMmanMapFlags(0x80000U)
        public val MAP_FIXED_NOREPLACE: SysMmanMapFlags = SysMmanMapFlags(0x100000U)
        public val MAP_FILE: SysMmanMapFlags = SysMmanMapFlags(0U)

        public val MAP_HUGE_16KB: SysMmanMapFlags = SysMmanMapFlags((14U.shl(26)))
        public val MAP_HUGE_64KB: SysMmanMapFlags = SysMmanMapFlags((16U.shl(26)))
        public val MAP_HUGE_512KB: SysMmanMapFlags = SysMmanMapFlags((19U.shl(26)))
        public val MAP_HUGE_1MB: SysMmanMapFlags = SysMmanMapFlags((20U.shl(26)))
        public val MAP_HUGE_2MB: SysMmanMapFlags = SysMmanMapFlags((21U.shl(26)))
        public val MAP_HUGE_8MB: SysMmanMapFlags = SysMmanMapFlags((23U.shl(26)))
        public val MAP_HUGE_16MB: SysMmanMapFlags = SysMmanMapFlags((24U.shl(26)))
        public val MAP_HUGE_32MB: SysMmanMapFlags = SysMmanMapFlags((25U.shl(26)))
        public val MAP_HUGE_256MB: SysMmanMapFlags = SysMmanMapFlags((28U.shl(26)))
        public val MAP_HUGE_512MB: SysMmanMapFlags = SysMmanMapFlags((29U.shl(26)))
        public val MAP_HUGE_1GB: SysMmanMapFlags = SysMmanMapFlags((30U.shl(26)))
        public val MAP_HUGE_2GB: SysMmanMapFlags = SysMmanMapFlags((31U.shl(26)))
        public val MAP_HUGE_16GB: SysMmanMapFlags = SysMmanMapFlags((34U.shl(26)))
    }
}
