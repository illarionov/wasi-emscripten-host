/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.include.sys

import androidx.annotation.IntDef
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * Memory protection bits for mmap
 *
 * <sys/mman.h>
 */
@IntDef(
    flag = true,
    value = [
        MemoryProtectionFlag.PROT_NONE,
        MemoryProtectionFlag.PROT_READ,
        MemoryProtectionFlag.PROT_WRITE,
        MemoryProtectionFlag.PROT_EXEC,
        MemoryProtectionFlag.PROT_GROWSDOWN,
        MemoryProtectionFlag.PROT_GROWSUP,
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
    AnnotationTarget.VALUE_PARAMETER,
)
internal annotation class SysMmanProt

public object MemoryProtectionFlag {
    public const val PROT_NONE: Int = 0
    public const val PROT_READ: Int = 1
    public const val PROT_WRITE: Int = 2
    public const val PROT_EXEC: Int = 4
    public const val PROT_GROWSDOWN: Int = 0x0100_0000
    public const val PROT_GROWSUP: Int = 0x0200_0000
}
