/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.common.api

@Suppress("IDENTIFIER_LENGTH")
public interface Logger {
    /**
     * Creates a logger with the specified tag
     */
    public fun withTag(tag: String): Logger

    /**
     * Send a VERBOSE(2) log message.
     */
    public fun v(throwable: Throwable? = null, message: () -> String)

    /**
     * Send a DEBUG(3) log message.
     */
    public fun d(throwable: Throwable? = null, message: () -> String)

    /**
     * Send a INFO(4) log message.
     */
    public fun i(throwable: Throwable? = null, message: () -> String)

    /**
     * Send a WARN(5) log message.
     */
    public fun w(throwable: Throwable? = null, message: () -> String)

    /**
     * Send a ERROR(6) log message.
     */
    public fun e(throwable: Throwable? = null, message: () -> String)

    /**
     * Send a ASSERT(7) log message.
     */
    public fun a(throwable: Throwable? = null, message: () -> String)

    public companion object : Logger {
        override fun withTag(tag: String): Logger = this
        override fun v(throwable: Throwable?, message: () -> String): Unit = Unit
        override fun d(throwable: Throwable?, message: () -> String): Unit = Unit
        override fun i(throwable: Throwable?, message: () -> String): Unit = Unit
        override fun w(throwable: Throwable?, message: () -> String): Unit = Unit
        override fun e(throwable: Throwable?, message: () -> String): Unit = Unit
        override fun a(throwable: Throwable?, message: () -> String): Unit = Unit
    }
}
