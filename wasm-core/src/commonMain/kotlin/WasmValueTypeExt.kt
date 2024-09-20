/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasm.core

import at.released.weh.wasm.core.WasmValueTypes.I32

@WasmValueType
public val POINTER: Int get() = I32

@WasmValueType
public inline fun witxPointer(
    @Suppress("UNUSED_PARAMETER") @WasmValueType type: Int,
): Int = POINTER
