/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.runner

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WasiTestsuiteArguments(
    val args: List<String> = emptyList(),
    val dirs: List<String> = emptyList(),
    val env: Map<String, String> = emptyMap(),

    @SerialName("exit_code")
    val exitCode: Int = 0,
    val stderr: String? = null,
    val stdout: String? = null,
)
