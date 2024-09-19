/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1

import at.released.weh.wasm.core.POINTER
import at.released.weh.wasm.core.WasmValueType

internal fun pointerToType(
    @Suppress("UNUSED_PARAMETER") type: WasiTypename,
): @WasmValueType Int = POINTER
