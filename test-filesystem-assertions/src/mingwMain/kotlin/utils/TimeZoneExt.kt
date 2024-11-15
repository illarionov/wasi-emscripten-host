/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import kotlinx.cinterop.toKStringFromUtf8
import platform.posix._putenv
import platform.posix.getenv
import platform.posix.tzset

public fun <R : Any> withTimeZone(
    timeZone: String,
    block: () -> R,
) {
    val oldTz = getenv("TZ")?.toKStringFromUtf8()
    setEnvOrThrow("TZ", timeZone)

    tzset()
    try {
        block()
    } finally {
        setEnvOrThrow("TZ", oldTz)
        tzset()
    }
}

private fun setEnvOrThrow(
    name: String,
    value: String?,
) {
    val errno = _putenv("$name=${value ?: ""}")
    if (errno != 0) {
        error("Can not modify environment variable")
    }
}
