/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime

public class AssertionFailedException(
    condition: String?,
    filename: String?,
    line: Int,
    func: String?,
) : RuntimeException(
    formatErrMsg(
        condition,
        filename,
        line,
        func,
    ),
) {
    private companion object {
        fun formatErrMsg(
            condition: String?,
            filename: String?,
            line: Int,
            func: String?,
        ): String = buildString {
            append("Assertion failed: ")
            append(condition ?: "``")
            append(",  at ")
            listOf(
                filename ?: "unknown filename",
                line.toString(),
                func ?: "unknown function",
            ).joinTo(this, ", ", prefix = "[", postfix = "]")
        }
    }
}
