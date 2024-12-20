/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.ext

import kotlin.LazyThreadSafetyMode.PUBLICATION

internal object Os {
    private val osName = System.getProperty("os.name") ?: "generic"
    val isWindows by lazy(PUBLICATION) { osName.contains("win", true) }
    val isLinux by lazy(PUBLICATION) { osName.findAnyOf(listOf("nix", "nux", "aix"), ignoreCase = true) != null }
}
