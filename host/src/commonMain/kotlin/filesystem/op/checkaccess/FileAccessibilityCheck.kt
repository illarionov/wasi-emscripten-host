/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.checkaccess

/**
 * File accessibility check(s) (F_OK, R_OK, W_OK, X_OK)
 */
public enum class FileAccessibilityCheck {
    /**
     * File exists and can be read
     */
    READABLE,

    /**
     * File exists and can be written
     */
    WRITEABLE,

    /**
     * File exists and can be executed
     */
    EXECUTABLE,
}
