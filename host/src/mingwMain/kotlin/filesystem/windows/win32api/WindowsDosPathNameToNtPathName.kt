/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtObjectManagerPath
import at.released.weh.filesystem.windows.win32api.errorcode.NtStatus
import at.released.weh.filesystem.windows.win32api.errorcode.NtStatus.NtStatusCode.STATUS_OBJECT_NAME_INVALID
import at.released.weh.filesystem.windows.win32api.errorcode.isSuccess
import at.released.weh.filesystem.windows.win32api.ext.readChars
import at.released.weh.host.platform.windows.RtlDosPathNameToNtPathName_U_WithStatus
import at.released.weh.host.platform.windows.RtlFreeUnicodeString
import at.released.weh.host.platform.windows.RtlInitUnicodeString
import at.released.weh.host.platform.windows.UNICODE_STRING
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.ptr
import kotlinx.cinterop.utf16

internal fun windowsDosPathNameToNtPathName(
    path: String,
): Either<ResolvePathError, WindowsNtObjectManagerPath> = memScoped {
    if (path.isEmpty()) {
        return PathError.EmptyPath("Path is empty").left()
    }

    val pathPtr = path.utf16.placeTo(this)
    val ntUnicodeString: UNICODE_STRING = alloc<UNICODE_STRING>()
    RtlInitUnicodeString(ntUnicodeString.ptr, null)

    val status = NtStatus(RtlDosPathNameToNtPathName_U_WithStatus(pathPtr, ntUnicodeString.ptr, null, null).toUInt())

    return if (status.isSuccess) {
        val ntPath = ntUnicodeString.Buffer!!.readChars(ntUnicodeString.Length.toInt() / 2).concatToString()
        RtlFreeUnicodeString(ntUnicodeString.ptr)
        WindowsNtObjectManagerPath.create(ntPath)
            .mapLeft { it as ResolvePathError }
    } else {
        status.toResolvePathError().left()
    }
}

private fun NtStatus.toResolvePathError(): ResolvePathError = when (raw) {
    STATUS_OBJECT_NAME_INVALID -> PathError.InvalidPathFormat("RtlDosPathNameToNtPathName_U failed: invalid argument")
    else -> PathError.IoError("Other error $this")
}
