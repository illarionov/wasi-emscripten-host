/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

import kotlin.jvm.JvmInline

/**
 * A file descriptor handle.
 */
@JvmInline
public value class Fd(
    public val fd: Int,
) {
    override fun toString(): String = "Fd($fd)"

    public companion object
}
