/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UNCHECKED_CAST")

package at.released.weh.bindings.chasm.ext

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import io.github.charlietap.chasm.embedding.shapes.Value
import io.github.charlietap.chasm.embedding.shapes.Value.Number

internal fun Value.asInt(): Int = (this as Number<Int>).value
internal fun Value.asUInt(): UInt = (this as Number<Int>).value.toUInt()
internal fun Value.asLong(): Long = (this as Number<Long>).value
internal fun Value.asULong(): ULong = (this as Number<Long>).value.toULong()

@IntWasmPtr
internal fun Value.asWasmAddr(): WasmPtr = this.asInt()
