/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.ext

import java.lang.reflect.Field

// Field.trySetAccessible is not available on Android
internal fun Field.trySetAccessibleCompat(): Boolean = try {
    this.isAccessible = true
    true
} catch (@Suppress("TooGenericExceptionCaught", "SwallowedException") re: RuntimeException) {
    false
}
