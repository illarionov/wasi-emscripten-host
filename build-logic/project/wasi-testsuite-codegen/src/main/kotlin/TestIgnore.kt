/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasi.testsuite.codegen

import java.io.Serializable

public data class TestIgnore(
    public val name: String,
    public val targets: Set<IgnoreTarget> = emptySet(),
) : Serializable {
    public enum class IgnoreTarget(val isStatic: Boolean) {
        APPLE(true),
        IOS(true),
        JVM(true),
        LINUX(true),
        MACOS(true),
        MINGW(true),
        NATIVE(true),
        JVM_ON_LINUX(false),
        JVM_ON_MACOS(false),
        JVM_ON_WINDOWS(false),
    }

    public companion object {
        private const val serialVersionUID: Long = 0L
    }
}
