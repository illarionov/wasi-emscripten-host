/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.io.bootstrap

/**
 * Workaround for https://youtrack.jetbrains.com/issue/KT-69709/
 */
public actual fun setupStdioBuffering() {
    // Not verified, assume it is not required
}

/**
 * Workaround for https://youtrack.jetbrains.com/issue/KT-69709/
 */
public actual fun flushStdioBuffers() {
    // Not verified, assume it is not required
}
