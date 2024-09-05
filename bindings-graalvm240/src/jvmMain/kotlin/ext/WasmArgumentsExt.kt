/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.ext

import at.released.weh.host.base.WasmPtr
import org.graalvm.polyglot.Value
import org.graalvm.wasm.WasmArguments

internal fun <P : Any?> Value.asWasmAddr(): WasmPtr<P> = WasmPtr(asInt())

internal fun Array<Any>.getArgAsInt(idx: Int): Int = WasmArguments.getArgument(this, idx) as Int
internal fun Array<Any>.getArgAsUint(idx: Int): UInt = (WasmArguments.getArgument(this, idx) as Int).toUInt()
internal fun Array<Any>.getArgAsLong(idx: Int): Long = WasmArguments.getArgument(this, idx) as Long
internal fun Array<Any>.getArgAsUlong(idx: Int): ULong = (WasmArguments.getArgument(this, idx) as Long)
    .toULong()

internal fun <P : Any?> Array<Any>.getArgAsWasmPtr(idx: Int): WasmPtr<P> = WasmPtr(
    WasmArguments.getArgument(this, idx) as Int,
)
