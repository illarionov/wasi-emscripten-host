/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.include

import androidx.annotation.IntDef
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * File accessibility check(s) (F_OK, R_OK, W_OK, X_OK)
 */
@Retention(SOURCE)
@IntDef(
    flag = true,
    value = [
        FileAccessibilityCheckFlag.F_OK,
        FileAccessibilityCheckFlag.R_OK,
        FileAccessibilityCheckFlag.W_OK,
        FileAccessibilityCheckFlag.X_OK,
        FileAccessibilityCheckFlag.MASK,
    ],
)
public annotation class FileAccessibilityCheck

public object FileAccessibilityCheckFlag {
    public const val F_OK: Int = Fcntl.F_OK
    public const val R_OK: Int = Fcntl.R_OK
    public const val W_OK: Int = Fcntl.W_OK
    public const val X_OK: Int = Fcntl.X_OK
    public const val MASK: Int = F_OK or R_OK or W_OK or X_OK
}
