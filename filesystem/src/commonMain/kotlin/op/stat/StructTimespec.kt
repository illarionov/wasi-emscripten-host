/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

public data class StructTimespec(
    val seconds: ULong,
    val nanoseconds: ULong,
) {
    override fun toString(): String {
        return "TS($seconds sec $nanoseconds nsec)"
    }
}

public val StructTimespec.timeMillis: ULong
    get(): ULong = seconds * 1000U + nanoseconds / 1_000_000U
