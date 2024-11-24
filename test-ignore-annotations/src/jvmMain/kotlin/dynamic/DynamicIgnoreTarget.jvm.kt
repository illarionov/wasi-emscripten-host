/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.ignore.annotations.dynamic

public actual fun DynamicIgnoreTarget.isCurrentSystem(): Boolean {
    val os = System.getProperty("os.name")
    return when (this) {
        DynamicIgnoreTarget.JVM_ON_LINUX -> os.findAnyOf(listOf("nix", "aix", "nux")) != null
        DynamicIgnoreTarget.JVM_ON_MACOS -> os.contains("mac", true)
        DynamicIgnoreTarget.JVM_ON_WINDOWS -> os.contains("win", true)
    }
}
