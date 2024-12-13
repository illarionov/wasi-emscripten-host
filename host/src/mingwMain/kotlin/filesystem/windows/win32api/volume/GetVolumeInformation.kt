/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.volume

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.ACCES
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.IO
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKStringFromUtf16
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.GetVolumeInformationByHandleW
import platform.windows.HANDLE
import platform.windows.MAX_PATH
import platform.windows.WCHARVar

internal fun HANDLE.getVolumeInformation(
    requestVolumeName: Boolean = false,
    requestVolumeSerialNumber: Boolean = false,
    requestMaximumComponentLength: Boolean = false,
    requestFileSystemFlags: Boolean = true,
    requestFileSystemName: Boolean = false,
): Either<GetVolumeInformationError, VolumeInformation> = memScoped {
    val bufferSize: UInt = MAX_PATH.toUInt() + 1U
    val (volumeNameSize, volumeNameBuffer) = if (requestVolumeName) {
        bufferSize to allocArray<WCHARVar>(bufferSize.toInt())
    } else {
        0U to null
    }
    val volumeSerialNumberBuffer = if (requestVolumeSerialNumber) {
        alloc<DWORDVar>()
    } else {
        null
    }
    val maximumComponentLength = if (requestMaximumComponentLength) {
        alloc<DWORDVar>()
    } else {
        null
    }
    val fileSystemFlags = if (requestFileSystemFlags) {
        alloc<DWORDVar>()
    } else {
        null
    }
    val (fileSystemNameSize, fileSystemNameBuffer) = if (requestFileSystemName) {
        bufferSize to allocArray<WCHARVar>(bufferSize.toInt())
    } else {
        0U to null
    }

    val result = GetVolumeInformationByHandleW(
        hFile = this@getVolumeInformation,
        lpVolumeNameBuffer = volumeNameBuffer,
        nVolumeNameSize = volumeNameSize,
        lpVolumeSerialNumber = volumeSerialNumberBuffer?.ptr,
        lpMaximumComponentLength = maximumComponentLength?.ptr,
        lpFileSystemFlags = fileSystemFlags?.ptr,
        lpFileSystemNameBuffer = fileSystemNameBuffer,
        nFileSystemNameSize = fileSystemNameSize,
    )

    return if (result != 0) {
        VolumeInformation(
            volumeName = volumeNameBuffer?.toKStringFromUtf16(),
            volumeSerialNumber = volumeSerialNumberBuffer?.value,
            maximumComponentLength = maximumComponentLength?.value,
            fileSystemFlags = fileSystemFlags?.let { FileSystemFlags(it.value) },
            fileSystemName = fileSystemNameBuffer?.toKStringFromUtf16(),
        ).right()
    } else {
        GetVolumeInformationError.create(Win32ErrorCode.getLast()).left()
    }
}

internal sealed class GetVolumeInformationError : FileSystemOperationError {
    internal data class AccessDenied(override val message: String) : GetVolumeInformationError() {
        override val errno: FileSystemErrno = ACCES
    }

    internal data class InvalidHandle(override val message: String) : GetVolumeInformationError() {
        override val errno: FileSystemErrno = BADF
    }

    internal data class OtherError(
        val code: Win32ErrorCode,
        override val message: String,
    ) : GetVolumeInformationError() {
        override val errno: FileSystemErrno = IO
    }

    internal companion object {
        fun create(code: Win32ErrorCode): GetVolumeInformationError = when (code.code.toInt()) {
            ERROR_ACCESS_DENIED -> AccessDenied("Can not read volume info: access denied")
            ERROR_INVALID_HANDLE -> InvalidHandle("Invalid handle")
            else -> OtherError(code, "Other error `$this`")
        }
    }
}
