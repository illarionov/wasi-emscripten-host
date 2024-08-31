/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.ext

import at.released.weh.host.base.WasmValueType
import com.dylibso.chicory.wasm.types.ValueType

internal val WasmValueType.chicory: ValueType
    get() = ValueType.forId(requireNotNull(opcode) { "Can not convert Wasi type without opcode" }.toInt())
