/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

internal object WasiArgsEnvironmentFunc {
    internal fun Map.Entry<String, String>.encodeEnvToWasi(): String {
        val key = cleanupProgramArgument(this.key).replace("=", "")
        val value = cleanupProgramArgument(value)
        return "$key=$value"
    }

    internal fun cleanupProgramArgument(
        arg: String,
    ): String = arg.replace("\u0000", "")
}
