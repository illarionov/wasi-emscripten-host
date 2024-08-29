/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("IDENTIFIER_LENGTH")

package at.released.weh.test.logger

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.common.api.Logger

@InternalWasiEmscriptenHostApi
public open class BaseLogger : Logger {
    override fun withTag(tag: String): Logger = this
    override fun v(throwable: Throwable?, message: () -> String): Unit = error("Should not be called")
    override fun d(throwable: Throwable?, message: () -> String): Unit = error("Should not be called")
    override fun i(throwable: Throwable?, message: () -> String): Unit = error("Should not be called")
    override fun w(throwable: Throwable?, message: () -> String): Unit = error("Should not be called")
    override fun e(throwable: Throwable?, message: () -> String): Unit = error("Should not be called")
    override fun a(throwable: Throwable?, message: () -> String): Unit = error("Should not be called")
}
