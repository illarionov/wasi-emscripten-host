/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NOTHING_TO_INLINE")

package at.released.weh.bindings.graalvm240.ext

import at.released.weh.host.base.IntWasmPtr
import at.released.weh.host.base.WasmPtr
import org.graalvm.polyglot.Value
import org.graalvm.wasm.WasmArguments

@IntWasmPtr
internal inline fun Value.asWasmAddr(): WasmPtr = asInt()

internal inline fun Array<Any>.getArgAsInt(idx: Int): Int = WasmArguments.getArgument(this, idx) as Int
internal inline fun Array<Any>.getArgAsUint(idx: Int): UInt = (WasmArguments.getArgument(this, idx) as Int).toUInt()
internal inline fun Array<Any>.getArgAsLong(idx: Int): Long = WasmArguments.getArgument(this, idx) as Long
internal inline fun Array<Any>.getArgAsUlong(idx: Int): ULong = (WasmArguments.getArgument(this, idx) as Long)
    .toULong()

@IntWasmPtr
internal inline fun Array<Any>.getArgAsWasmPtr(idx: Int): WasmPtr = WasmArguments.getArgument(this, idx) as Int
