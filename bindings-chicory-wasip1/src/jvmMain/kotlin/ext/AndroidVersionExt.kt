/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.ext

@Suppress("PrivateApi", "MagicNumber", "TooGenericExceptionCaught", "SwallowedException")
internal fun isJvmOrAndroidMinApi34(): Boolean {
    try {
        val sdkIntField = Class.forName("android.os.Build\$VERSION").getField("SDK_INT")
        val version = sdkIntField.getInt(null) as? Int ?: 0
        return when {
            version == 0 -> true // Android unit tests
            else -> version >= 34
        }
    } catch (ex: Exception) {
        // is JVM
        return true
    }
}
