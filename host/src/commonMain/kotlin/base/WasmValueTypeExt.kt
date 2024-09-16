/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base

import at.released.weh.host.base.WasmValueTypes.I32
import at.released.weh.host.wasi.preview1.type.WasiTypename

@WasmValueType
public val POINTER: Int get() = I32

public fun pointerToType(
    @Suppress("UNUSED_PARAMETER") @WasmValueType type: Int,
): @WasmValueType Int = POINTER

public fun pointerToType(
    @Suppress("UNUSED_PARAMETER") type: WasiTypename,
): @WasmValueType Int = POINTER
