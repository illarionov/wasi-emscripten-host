/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.include

@Suppress("PropertyName", "ConstructorParameterNaming")
public data class StructTimespec(
    val tv_sec: time_t,
    val tv_nsec: ULong,
) {
    override fun toString(): String {
        return "TS($tv_sec sec $tv_nsec nsec)"
    }
}

@Suppress("TYPEALIAS_NAME_INCORRECT_CASE")
public typealias time_t = ULong

public val StructTimespec.timeMillis: ULong
    get(): ULong = tv_sec * 1000U + tv_nsec / 1_000_000U
