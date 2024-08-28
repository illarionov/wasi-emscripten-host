/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.include

import at.released.weh.common.api.SqliteUintBitMask
import at.released.weh.common.api.or
import kotlin.jvm.JvmInline

/**
 * File accessibility check(s) (F_OK, R_OK, W_OK, X_OK)
 */
@JvmInline
public value class FileAccessibilityCheck(
    public override val mask: UInt,
) : SqliteUintBitMask<FileAccessibilityCheck> {
    override val newInstance: (UInt) -> FileAccessibilityCheck get() = ::FileAccessibilityCheck

    override fun toString(): String = "0${mask.toString(8)}"

    public companion object {
        public val F_OK: FileAccessibilityCheck = FileAccessibilityCheck(Fcntl.F_OK)
        public val R_OK: FileAccessibilityCheck = FileAccessibilityCheck(Fcntl.R_OK)
        public val W_OK: FileAccessibilityCheck = FileAccessibilityCheck(Fcntl.W_OK)
        public val X_OK: FileAccessibilityCheck = FileAccessibilityCheck(Fcntl.X_OK)
        public val MASK: FileAccessibilityCheck = F_OK or R_OK or W_OK or X_OK
    }
}
