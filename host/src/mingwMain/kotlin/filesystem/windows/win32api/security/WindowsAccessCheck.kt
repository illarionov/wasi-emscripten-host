/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.security

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.EXECUTABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.READABLE
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck.WRITEABLE
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.AccessCheck
import platform.windows.BOOLVar
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.FILE_ALL_ACCESS
import platform.windows.FILE_GENERIC_EXECUTE
import platform.windows.FILE_GENERIC_READ
import platform.windows.FILE_GENERIC_WRITE
import platform.windows.GENERIC_EXECUTE
import platform.windows.GENERIC_MAPPING
import platform.windows.GENERIC_READ
import platform.windows.GENERIC_WRITE
import platform.windows.HANDLE
import platform.windows.MapGenericMask
import platform.windows.PSECURITY_DESCRIPTOR

internal fun windowsAccessCheck(
    securityDescriptor: PSECURITY_DESCRIPTOR,
    clientToken: HANDLE,
    desiredAccess: Set<FileAccessibilityCheck>,
): Either<CheckAccessError, Unit> = memScoped {
    val genericMapping: GENERIC_MAPPING = alloc<GENERIC_MAPPING>().apply {
        GenericRead = FILE_GENERIC_READ.toUInt()
        GenericWrite = FILE_GENERIC_WRITE.toUInt()
        GenericExecute = FILE_GENERIC_EXECUTE.toUInt()
        GenericAll = FILE_ALL_ACCESS.toUInt()
    }
    val desiredAccessMask: UIntVar = alloc<UIntVar>().apply { value = desiredAccess.toDesiredAccessMask() }
    val privilegeSetLength: UIntVar = alloc<UIntVar>().apply { value = 0U }
    val grantAccess: UIntVar = alloc<UIntVar>().apply { value = 0U }
    val accessStatus: BOOLVar = alloc<BOOLVar>().apply { value = 0 }

    MapGenericMask(desiredAccessMask.ptr, genericMapping.ptr)

    val result = AccessCheck(
        securityDescriptor,
        clientToken,
        desiredAccessMask.value,
        genericMapping.ptr,
        null,
        privilegeSetLength.ptr,
        grantAccess.ptr,
        accessStatus.ptr,
    )

    return when {
        result == 0 -> Win32ErrorCode.getLast().toCheckAccessError().left()
        accessStatus.value != 0 -> Unit.right()
        else -> AccessDenied("Access not granted").left()
    }
}

private fun Set<FileAccessibilityCheck>.toDesiredAccessMask(): UInt = fold(0U) { mask, check ->
    mask or when (check) {
        READABLE -> GENERIC_READ.toUInt()
        WRITEABLE -> GENERIC_WRITE.toUInt()
        EXECUTABLE -> GENERIC_EXECUTE.toUInt()
    }
}

private fun Win32ErrorCode.toCheckAccessError(): CheckAccessError = when (this.code.toInt()) {
    ERROR_ACCESS_DENIED -> AccessDenied("Access denied")
    ERROR_FILE_NOT_FOUND -> NotDirectory("file not found")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Invalid parameters")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    else -> InvalidArgument("Other error: $this")
}
