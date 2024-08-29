/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.io.bootstrap

public object TestEnv {
    public fun prepare() {
        setupInputStreamBuffering()
    }

    public fun cleanup() {
        flushBuffers()
    }
}

/**
 * Workaround for https://youtrack.jetbrains.com/issue/KT-69709/
 */
public expect fun setupInputStreamBuffering()

/**
 * Workaround for https://youtrack.jetbrains.com/issue/KT-69709/
 */
public expect fun flushBuffers()
