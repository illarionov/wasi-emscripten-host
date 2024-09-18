/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.wasi.preview1.type

import at.released.weh.wasi.filesystem.common.Errno
import at.released.weh.wasi.preview1.type.WasiValueTypes.U16
import at.released.weh.wasm.core.WasmValueType

@WasmValueType
public val Errno.Companion.wasmValueType: Int get() = U16