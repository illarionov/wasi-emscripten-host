/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import kotlinx.io.IOException

public class WindowsIoException : IOException {
    public val lastError: UInt?

    public constructor(lastError: UInt? = null) : super() {
        this.lastError = lastError
    }

    public constructor(messagePrefix: String, lastError: UInt? = null) : super(
        messagePrefix + (lastError?.let { ": 0x${it.toString(16)}" } ?: ""),
    ) {
        this.lastError = lastError
    }
}
