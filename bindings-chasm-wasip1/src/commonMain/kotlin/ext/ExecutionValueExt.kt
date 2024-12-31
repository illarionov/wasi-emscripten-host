/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UNCHECKED_CAST")

package at.released.weh.bindings.chasm.ext

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import io.github.charlietap.chasm.embedding.shapes.Value
import io.github.charlietap.chasm.embedding.shapes.Value.Number

@InternalWasiEmscriptenHostApi
public fun Value.asByte(): Byte = (this as Number<Int>).value.toByte()

@InternalWasiEmscriptenHostApi
public fun Value.asShort(): Short = (this as Number<Int>).value.toShort()

@InternalWasiEmscriptenHostApi
public fun Value.asInt(): Int = (this as Number<Int>).value

@InternalWasiEmscriptenHostApi
public fun Value.asUInt(): UInt = (this as Number<Int>).value.toUInt()

@InternalWasiEmscriptenHostApi
public fun Value.asLong(): Long = (this as Number<Long>).value

@InternalWasiEmscriptenHostApi
public fun Value.asULong(): ULong = (this as Number<Long>).value.toULong()

@IntWasmPtr
@InternalWasiEmscriptenHostApi
public fun Value.asWasmAddr(): WasmPtr = this.asInt()
