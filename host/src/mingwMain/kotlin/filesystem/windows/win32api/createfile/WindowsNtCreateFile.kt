/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("COMMENTED_OUT_CODE")

package at.released.weh.filesystem.windows.win32api.createfile

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.windows.win32api.errorcode.NtStatus
import at.released.weh.filesystem.windows.win32api.errorcode.NtStatus.NtStatusCode
import at.released.weh.filesystem.windows.win32api.errorcode.isSuccess
import at.released.weh.filesystem.windows.win32api.model.IoStatusBlockInformation
import at.released.weh.host.platform.windows.IO_STATUS_BLOCK
import at.released.weh.host.platform.windows.NtCreateFile
import at.released.weh.host.platform.windows.OBJECT_ATTRIBUTES
import at.released.weh.host.platform.windows.OBJ_CASE_INSENSITIVE
import at.released.weh.host.platform.windows.RtlInitUnicodeString
import at.released.weh.host.platform.windows.UNICODE_STRING
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CValues
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.placeTo
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.utf16
import kotlinx.cinterop.value
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_DIRECTORY_FILE
import platform.windows.FILE_GENERIC_WRITE
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_OPEN
import platform.windows.FILE_OPEN_REPARSE_POINT
import platform.windows.FILE_RANDOM_ACCESS
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_SHARE_DELETE
import platform.windows.FILE_SHARE_READ
import platform.windows.FILE_SHARE_WRITE
import platform.windows.FILE_SYNCHRONOUS_IO_ALERT
import platform.windows.FILE_TRAVERSE
import platform.windows.HANDLE
import platform.windows.HANDLEVar
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.LARGE_INTEGER

internal fun windowsNtOpenDirectory(
    ntPath: NtPath,
    desiredAccess: Int = FILE_LIST_DIRECTORY or FILE_READ_ATTRIBUTES or FILE_TRAVERSE,
    followSymlinks: Boolean = true,
): Either<OpenError, HANDLE> {
    val createOptions = if (followSymlinks) {
        FILE_DIRECTORY_FILE
    } else {
        FILE_DIRECTORY_FILE or FILE_OPEN_REPARSE_POINT
    }

    return windowsNtCreateFile(
        ntPath = ntPath,
        desiredAccess = desiredAccess,
        fileAttributes = FILE_ATTRIBUTE_DIRECTORY,
        createDisposition = FILE_OPEN,
        createOptions = createOptions,
        caseSensitive = true,
    ).mapLeft(NtCreateFileResult::toOpenError)
}

internal fun windowsNtCreateFile(
    ntPath: NtPath,
    desiredAccess: Int = FILE_GENERIC_WRITE,
    fileAttributes: Int = FILE_ATTRIBUTE_NORMAL,
    shareAccess: Int = FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE,
    createDisposition: Int = FILE_OPEN,
    createOptions: Int = FILE_RANDOM_ACCESS or FILE_SYNCHRONOUS_IO_ALERT,
    caseSensitive: Boolean = true,
): Either<NtCreateFileResult, HANDLE> = memScoped {
    val handle: HANDLEVar = alloc<HANDLEVar>().apply {
        this.value = INVALID_HANDLE_VALUE
    }
    val allocationSize: LARGE_INTEGER = alloc<LARGE_INTEGER>().apply {
        this.QuadPart = 0
    }
    val ioStatusBlock: IO_STATUS_BLOCK = alloc()
    val pathUtf16: CValues<UShortVar> = ntPath.pathString.utf16
    val pathBuffer: CPointer<UShortVar> = pathUtf16.placeTo(this@memScoped)

    val objectName: UNICODE_STRING = alloc<UNICODE_STRING>()
    RtlInitUnicodeString(objectName.ptr, pathBuffer)

    val objectAttributes = alloc<OBJECT_ATTRIBUTES>().apply {
        Length = sizeOf<OBJECT_ATTRIBUTES>().toUInt()
        RootDirectory = ntPath.handle
        ObjectName = objectName.ptr
        Attributes = getObjectAttributes(caseSensitive)
        SecurityDescriptor = null
        SecurityQualityOfService = null
    }

    val status: NtStatus = NtCreateFile(
        FileHandle = handle.ptr,
        DesiredAccess = desiredAccess.toUInt(),
        ObjectAttributes = objectAttributes.ptr,
        IoStatusBlock = ioStatusBlock.ptr,
        AllocationSize = allocationSize.ptr,
        FileAttributes = fileAttributes.toUInt(),
        ShareAccess = shareAccess.toUInt(),
        CreateDisposition = createDisposition.toUInt(),
        CreateOptions = createOptions.toUInt(),
        EaBuffer = null,
        EaLength = 0U,
    ).let {
        NtStatus(it.toUInt())
    }

    if (status.isSuccess) {
        val handleValue: HANDLE = handle.value ?: error("Handle is null")
        handleValue.right()
    } else {
        NtCreateFileResult(status, ioStatusBlock.Information).left()
    }
}

private fun getObjectAttributes(
    caseSensitive: Boolean,
): UInt {
    return if (!caseSensitive) {
        OBJ_CASE_INSENSITIVE.toUInt()
    } else {
        0U
    }
}

internal data class NtCreateFileResult(
    val status: NtStatus,
    val ioStatusBlockInformation: ULong,
)

@Suppress("CyclomaticComplexMethod")
internal fun NtCreateFileResult.toOpenError(): OpenError {
    when (ioStatusBlockInformation.toUInt()) {
        IoStatusBlockInformation.FILE_EXISTS -> return Exists("File exists")
        IoStatusBlockInformation.FILE_DOES_NOT_EXIST -> return NoEntry("File not exists")
    }

    return when (status.raw) {
        // XXX: find more possible error codes
        NtStatus.STATUS_INVALID_PARAMETER -> InvalidArgument("NtCreateFile failed: invalid argument")
        NtStatus.STATUS_UNSUCCESSFUL -> IoError("Other error $status")
        NtStatusCode.STATUS_ACCESS_DENIED -> AccessDenied("Access denied")
        NtStatusCode.STATUS_DELETE_PENDING -> Exists("Delete pending")
        NtStatusCode.STATUS_NOT_A_DIRECTORY -> NotDirectory("Not a directory")
        NtStatusCode.STATUS_NOT_IMPLEMENTED -> NotSupported("Operation not supported")
        NtStatusCode.STATUS_OBJECT_NAME_COLLISION -> Exists("File exists")
        NtStatusCode.STATUS_OBJECT_NAME_INVALID -> InvalidArgument("Invalid filename")
        NtStatusCode.STATUS_OBJECT_NAME_NOT_FOUND -> NoEntry("Name not found")
        NtStatusCode.STATUS_OBJECT_PATH_NOT_FOUND -> NoEntry("Path not found")
        NtStatusCode.STATUS_OBJECT_PATH_SYNTAX_BAD -> InvalidArgument("Unsupported path format")
        else -> IoError("Other error $status")
    }
}
