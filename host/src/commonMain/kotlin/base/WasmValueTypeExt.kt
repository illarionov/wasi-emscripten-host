/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base

import at.released.weh.host.base.WasmValueType.I32
import at.released.weh.host.wasi.preview1.type.WasiTypename

public val POINTER: WasmValueType get() = I32

public val WasmValueType.pointer: WasmValueType
    get() = POINTER

public val WasiTypename.pointer: WasmValueType
    get() = POINTER
