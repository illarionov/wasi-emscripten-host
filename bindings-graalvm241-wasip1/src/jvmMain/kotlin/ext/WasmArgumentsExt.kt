/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NOTHING_TO_INLINE")

package at.released.weh.bindings.graalvm241.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import org.graalvm.polyglot.Value
import org.graalvm.wasm.WasmArguments

@InternalWasiEmscriptenHostApi
@IntWasmPtr
public inline fun Value.asWasmAddr(): WasmPtr = asInt()

@InternalWasiEmscriptenHostApi
public inline fun Array<Any>.getArgAsByte(idx: Int): Byte = (WasmArguments.getArgument(this, idx) as Int).toByte()

@InternalWasiEmscriptenHostApi
public inline fun Array<Any>.getArgAsShort(idx: Int): Short = (WasmArguments.getArgument(this, idx) as Int).toShort()

@InternalWasiEmscriptenHostApi
public inline fun Array<Any>.getArgAsInt(idx: Int): Int = WasmArguments.getArgument(this, idx) as Int

@InternalWasiEmscriptenHostApi
public inline fun Array<Any>.getArgAsUint(idx: Int): UInt = (WasmArguments.getArgument(this, idx) as Int).toUInt()

@InternalWasiEmscriptenHostApi
public inline fun Array<Any>.getArgAsLong(idx: Int): Long = WasmArguments.getArgument(this, idx) as Long

@InternalWasiEmscriptenHostApi
public inline fun Array<Any>.getArgAsUlong(idx: Int): ULong = (WasmArguments.getArgument(this, idx) as Long)
    .toULong()

@IntWasmPtr
@InternalWasiEmscriptenHostApi
public inline fun Array<Any>.getArgAsWasmPtr(idx: Int): WasmPtr = WasmArguments.getArgument(this, idx) as Int
