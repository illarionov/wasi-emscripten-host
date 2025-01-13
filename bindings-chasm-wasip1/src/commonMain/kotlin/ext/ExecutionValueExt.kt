/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NOTHING_TO_INLINE")

package at.released.weh.bindings.chasm.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue
import io.github.charlietap.chasm.executor.runtime.value.NumberValue

@InternalWasiEmscriptenHostApi
public inline fun ExecutionValue.asByte(): Byte = (this as NumberValue.I32).value.toByte()

@InternalWasiEmscriptenHostApi
public inline fun ExecutionValue.asShort(): Short = (this as NumberValue.I32).value.toShort()

@InternalWasiEmscriptenHostApi
public inline fun ExecutionValue.asInt(): Int = (this as NumberValue.I32).value

@InternalWasiEmscriptenHostApi
public inline fun ExecutionValue.asUInt(): UInt = (this as NumberValue.I32).value.toUInt()

@InternalWasiEmscriptenHostApi
public inline fun ExecutionValue.asLong(): Long = (this as NumberValue.I64).value

@InternalWasiEmscriptenHostApi
public inline fun ExecutionValue.asULong(): ULong = (this as NumberValue.I64).value.toULong()

@IntWasmPtr
@InternalWasiEmscriptenHostApi
public inline fun ExecutionValue.asWasmAddr(): WasmPtr = this.asInt()
