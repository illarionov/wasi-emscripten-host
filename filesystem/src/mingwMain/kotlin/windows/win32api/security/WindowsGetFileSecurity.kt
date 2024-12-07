/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.alignOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.ERROR_INSUFFICIENT_BUFFER
import platform.windows.GetFileSecurityW
import platform.windows.PSECURITY_DESCRIPTOR
import platform.windows.SECURITY_DESCRIPTOR
import platform.windows.SECURITY_INFORMATION

internal fun windowsGetFileSecurity(
    path: RealPath,
    requestedInformation: SECURITY_INFORMATION,
    heap: NativePlacement,
): Either<Win32ErrorCode, PSECURITY_DESCRIPTOR> = memScoped {
    val lengthNeeded: DWORDVar = alloc()

    if (GetFileSecurityW(path, requestedInformation, null, 0U, lengthNeeded.ptr) == 0) {
        val lastError = Win32ErrorCode.getLast()
        if (lastError.code != ERROR_INSUFFICIENT_BUFFER.toUInt()) {
            return lastError.left()
        }
    }

    val newLength = lengthNeeded.value
    val descriptor: SECURITY_DESCRIPTOR = heap.alloc(newLength.toInt(), alignOf<SECURITY_DESCRIPTOR>()).reinterpret()
    return if (
        GetFileSecurityW(
            lpFileName = path,
            RequestedInformation = requestedInformation,
            pSecurityDescriptor = descriptor.ptr,
            nLength = newLength,
            lpnLengthNeeded = lengthNeeded.ptr,
        ) != 0
    ) {
        return descriptor.ptr.right()
    } else {
        Win32ErrorCode.getLast().left()
    }
}
