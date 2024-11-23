/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api

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
import at.released.weh.filesystem.platform.windows.IO_STATUS_BLOCK
import at.released.weh.filesystem.platform.windows.NtCreateFile
import at.released.weh.filesystem.platform.windows.OBJECT_ATTRIBUTES
import at.released.weh.filesystem.platform.windows.OBJ_CASE_INSENSITIVE
import at.released.weh.filesystem.platform.windows.OBJ_OPENLINK
import at.released.weh.filesystem.platform.windows.RtlInitUnicodeString
import at.released.weh.filesystem.platform.windows.UNICODE_STRING
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.win32api.ext.WIN32_NT_KERNEL_DEVICES_PREFIX
import at.released.weh.filesystem.windows.win32api.model.IoStatusBlockInformation
import at.released.weh.filesystem.windows.win32api.model.errorcode.NtStatus
import at.released.weh.filesystem.windows.win32api.model.errorcode.NtStatus.NtStatusCode
import at.released.weh.filesystem.windows.win32api.model.errorcode.isSuccess
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
import platform.windows.FILE_ATTRIBUTE_NORMAL
import platform.windows.FILE_GENERIC_WRITE
import platform.windows.FILE_OPEN
import platform.windows.FILE_RANDOM_ACCESS
import platform.windows.FILE_SHARE_DELETE
import platform.windows.FILE_SHARE_READ
import platform.windows.FILE_SHARE_WRITE
import platform.windows.FILE_SYNCHRONOUS_IO_ALERT
import platform.windows.HANDLE
import platform.windows.HANDLEVar
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.LARGE_INTEGER
import platform.windows.PathIsRelativeW

internal fun ntCreateFileEx(
    rootHandle: HANDLE?,
    path: RealPath,
    desiredAccess: Int = FILE_GENERIC_WRITE,
    fileAttributes: Int = FILE_ATTRIBUTE_NORMAL,
    shareAccess: Int = FILE_SHARE_READ or FILE_SHARE_WRITE or FILE_SHARE_DELETE,
    createDisposition: Int = FILE_OPEN,
    createOptions: Int = FILE_RANDOM_ACCESS or FILE_SYNCHRONOUS_IO_ALERT,
    followSymlinks: Boolean = true,
    caseSensitive: Boolean = true,
): Either<OpenError, HANDLE> = memScoped {
    val pathIsRelative = PathIsRelativeW(path) != 0 // XXX need own version without limit of MAX_PATH

    val pathWithNamespace = if (!pathIsRelative && !path.startsWith(WIN32_NT_KERNEL_DEVICES_PREFIX)) {
        WIN32_NT_KERNEL_DEVICES_PREFIX + path
    } else {
        path
    }

    val handle: HANDLEVar = alloc<HANDLEVar>().apply {
        this.value = INVALID_HANDLE_VALUE
    }
    val allocationSize: LARGE_INTEGER = alloc<LARGE_INTEGER>().apply {
        this.QuadPart = 0
    }
    val ioStatusBlock: IO_STATUS_BLOCK = alloc()
    val pathUtf16: CValues<UShortVar> = pathWithNamespace.utf16
    val pathBuffer: CPointer<UShortVar> = pathUtf16.placeTo(this@memScoped)

    val objectName: UNICODE_STRING = alloc<UNICODE_STRING>()
    RtlInitUnicodeString(objectName.ptr, pathBuffer)

    val objectAttributes = alloc<OBJECT_ATTRIBUTES>().apply {
        Length = sizeOf<OBJECT_ATTRIBUTES>().toUInt()
        RootDirectory = rootHandle
        ObjectName = objectName.ptr
        Attributes = getObjectAttributes(caseSensitive, followSymlinks)
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
        ntCreateFileExStatusToOpenError(status, ioStatusBlock).left()
    }
}

private fun getObjectAttributes(
    caseSensitive: Boolean,
    followSymlinks: Boolean,
): UInt {
    var flags = if (!caseSensitive) {
        OBJ_CASE_INSENSITIVE.toUInt()
    } else {
        0U
    }
    if (!followSymlinks) {
        // TODO: OBJ_DONT_REPARSE?
        flags = flags or OBJ_OPENLINK.toUInt()
    }
    return flags
}

private fun ntCreateFileExStatusToOpenError(
    status: NtStatus,
    ioStatusBlock: IO_STATUS_BLOCK,
): OpenError {
    when (ioStatusBlock.Information.toUInt()) {
        IoStatusBlockInformation.FILE_EXISTS -> return Exists("File exists")
        IoStatusBlockInformation.FILE_DOES_NOT_EXIST -> return NoEntry("File not exists")
    }

    return when (status.raw) {
        // TODO: find more possible error codes
        NtStatus.STATUS_INVALID_PARAMETER -> InvalidArgument("NtCreateFile failed: invalid argument")
        NtStatus.STATUS_UNSUCCESSFUL -> IoError("Other error $status")
        NtStatusCode.STATUS_ACCESS_DENIED -> AccessDenied("Access denied")
        NtStatusCode.STATUS_NOT_A_DIRECTORY -> NotDirectory("Not a directory")
        NtStatusCode.STATUS_NOT_IMPLEMENTED -> NotSupported("Operation not supported")
        NtStatusCode.STATUS_OBJECT_NAME_INVALID -> InvalidArgument("Invalid filename")
        NtStatusCode.STATUS_OBJECT_NAME_NOT_FOUND -> NoEntry("Name not found")
        NtStatusCode.STATUS_OBJECT_PATH_NOT_FOUND -> NoEntry("Path not found")
        NtStatusCode.STATUS_OBJECT_PATH_SYNTAX_BAD -> InvalidArgument("Unsupported path format")
        else -> IoError("Other error $status")
    }
}
