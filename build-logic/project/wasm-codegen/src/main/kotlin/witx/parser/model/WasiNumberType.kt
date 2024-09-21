/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.gradle.wasm.codegen.witx.parser.model

internal sealed interface WasiNumberType {
    enum class UnsignedNumber : WasiNumberType {
        U8,
        U16,
        U32,
        U64,
    }

    enum class SignedNumber : WasiNumberType {
        S8,
        S16,
        S32,
        S64,
    }
}
