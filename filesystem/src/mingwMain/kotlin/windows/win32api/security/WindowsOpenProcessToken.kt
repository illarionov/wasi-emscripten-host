/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.GetCurrentProcess
import platform.windows.HANDLE
import platform.windows.HANDLEVar
import platform.windows.OpenProcessToken
import platform.windows.TOKEN_QUERY

internal fun windowsOpenProcessToken(
    processHandle: HANDLE = GetCurrentProcess()!!,
    desiredAccess: UInt = TOKEN_QUERY.toUInt(),
): Either<Win32ErrorCode, HANDLE> = memScoped {
    val resultHandle: HANDLEVar = alloc()
    return if (OpenProcessToken(processHandle, desiredAccess, resultHandle.ptr) != 0) {
        resultHandle.value!!.right()
    } else {
        Win32ErrorCode.getLast().left()
    }
}
