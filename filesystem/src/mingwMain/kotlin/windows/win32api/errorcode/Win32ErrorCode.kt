/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.errorcode

import platform.windows.GetLastError

internal value class Win32ErrorCode(
    val code: UInt,
) {
    override fun toString(): String {
        return "Win32Error(0x${code.toString(16).padStart(8, '0')} `${getErrorMessage()}`)"
    }

    public companion object {
        fun getLast(): Win32ErrorCode = Win32ErrorCode(GetLastError())
    }
}
