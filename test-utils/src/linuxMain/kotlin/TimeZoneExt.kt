/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils

import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.errno
import platform.posix.getenv
import platform.posix.setenv
import platform.posix.tzset
import platform.posix.unsetenv

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
        if (oldTz == null) {
            val unsetEnvResult = unsetenv("TZ")
            if (unsetEnvResult < 0) {
                error("Can not unset TZ. Errno: $errno")
            }
        } else {
            setEnvOrThrow("TZ", oldTz)
        }
        tzset()
    }
}

private fun setEnvOrThrow(
    name: String,
    value: String,
    replace: Boolean = true,
) {
    val setEnvResult = setenv(name, value, if (replace) 1 else 0)
    if (setEnvResult < 0) {
        error("Can not set $name to `$value`. Errno: $errno")
    }
}
