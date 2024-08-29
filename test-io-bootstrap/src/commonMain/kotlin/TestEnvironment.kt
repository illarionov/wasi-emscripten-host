/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.io.bootstrap

/**
 * A helper used to prepare the test environment.
 * Sets up stdout and stderr buffering so that test runner can capture full test output.
 * Used with tests that print something to standard output or stderr.
 *
 * Workaround for https://youtrack.jetbrains.com/issue/KT-69709/
 */
public object TestEnvironment {
    /**
     * Method to be called before each test. Turns off buffering of standard input/output.
     */
    public fun prepare() {
        setupStdioBuffering()
    }

    /**
     * Method be called after each test. Flushes stdout/stderr.
     */
    public fun cleanup() {
        flushStdioBuffers()
    }

    public inline fun <R : Any> use(
        crossinline block: () -> R,
    ) {
        prepare()
        try {
            block()
        } finally {
            cleanup()
        }
    }
}

/**
 * Turns off buffering of standard input/output. Intended to be called иуащку each test.
 *
 * Can be used as an additional level of protection, or as an alternative to calling [flushStdioBuffers] after each
 * test (assuming that buffering will not be turned back on in the test code).
 *
 * May be used as a workaround for https://youtrack.jetbrains.com/issue/KT-69709/
 */
public expect fun setupStdioBuffering()

/**
 * Flushes the standard output and standard error. Intended to be called after each test.
 *
 * Workaround for https://youtrack.jetbrains.com/issue/KT-69709/
 */
public expect fun flushStdioBuffers()
