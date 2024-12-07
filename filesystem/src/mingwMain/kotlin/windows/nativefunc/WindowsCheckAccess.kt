/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.op.checkaccess.FileAccessibilityCheck
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import at.released.weh.filesystem.windows.win32api.filepath.toResolveRelativePathError
import at.released.weh.filesystem.windows.win32api.security.windowsAccessCheck
import at.released.weh.filesystem.windows.win32api.security.windowsGetFileSecurity
import at.released.weh.filesystem.windows.win32api.security.windowsOpenProcessToken
import at.released.weh.filesystem.windows.win32api.security.windowsOpenThreadToken
import at.released.weh.filesystem.windows.win32api.volume.GetVolumeInformationError
import at.released.weh.filesystem.windows.win32api.volume.getVolumeInformation
import kotlinx.cinterop.memScoped
import platform.windows.DACL_SECURITY_INFORMATION
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.GROUP_SECURITY_INFORMATION
import platform.windows.HANDLE
import platform.windows.OWNER_SECURITY_INFORMATION
import platform.windows.PSECURITY_DESCRIPTOR

internal fun windowsCheckAccessFd(
    handle: HANDLE,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean,
): Either<CheckAccessError, Unit> = either {
    if (FileAccessibilityCheck.WRITEABLE in mode) {
        if (handle.hasReadonlyAttribute().bind()) {
            raise(AccessDenied("Read-only attribute set"))
        }
        val volumeIsReadOnly = handle.getVolumeInformation(requestFileSystemFlags = true)
            .map { it.fileSystemFlags!!.isReadOnly }
            .getOrElse { volumeError: GetVolumeInformationError ->
                if (volumeError is GetVolumeInformationError.AccessDenied) {
                    // Ignore
                    false
                } else {
                    raise(volumeError.toCheckAccessError())
                }
            }
        if (volumeIsReadOnly) {
            raise(AccessDenied("Disk volume is read-only"))
        }
    }

    val path = handle.getFinalPath()
        .mapLeft(GetFinalPathError::toResolveRelativePathError)
        .bind()

    // TODO ACL should be checked on handle using GetSecurityInfo?
    checkFileAcl(path, mode, useEffectiveUserId).bind()
}

private fun checkFileAcl(
    path: RealPath,
    mode: Set<FileAccessibilityCheck>,
    useEffectiveUserId: Boolean,
): Either<CheckAccessError, Unit> = either {
    memScoped {
        val fileSecurityDescriptor: PSECURITY_DESCRIPTOR = windowsGetFileSecurity(
            path,
            (DACL_SECURITY_INFORMATION or OWNER_SECURITY_INFORMATION or GROUP_SECURITY_INFORMATION).toUInt(),
            this,
        )
            .mapLeft(Win32ErrorCode::getFileSecurityErrorToCheckAccessError)
            .bind()

        val currentThreadToken: HANDLE? = windowsOpenThreadToken(openAsSelf = useEffectiveUserId)
            .mapLeft(Win32ErrorCode::getFileSecurityErrorToCheckAccessError)
            .getOrElse { null }

        // XXX useEffectiveUserId not used
        val token = currentThreadToken
            ?: windowsOpenProcessToken()
                .mapLeft(Win32ErrorCode::getFileSecurityErrorToCheckAccessError)
                .bind()

        try {
            windowsAccessCheck(fileSecurityDescriptor, token, mode)
        } finally {
            token.close()
        }
    }
}

private fun HANDLE.hasReadonlyAttribute(): Either<CheckAccessError, Boolean> {
    return getFileAttributeTagInfo()
        .mapLeft(StatError::toCheckAccessError)
        .map { it.fileAttributes.isReadOnly }
}

private fun Win32ErrorCode.getFileSecurityErrorToCheckAccessError(): CheckAccessError = when (this.code.toInt()) {
    ERROR_ACCESS_DENIED -> AccessDenied("Access denied")
    ERROR_FILE_NOT_FOUND -> NoEntry("file not found")
    ERROR_INVALID_PARAMETER -> InvalidArgument("Invalid parameters")
    ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
    else -> InvalidArgument("Other error: $this")
}

private fun StatError.toCheckAccessError(): CheckAccessError = if (this is CheckAccessError) {
    this
} else {
    IoError(this.message)
}

private fun GetVolumeInformationError.toCheckAccessError(): CheckAccessError = when (this) {
    is GetVolumeInformationError.AccessDenied -> AccessDenied("Access denied")
    is GetVolumeInformationError.InvalidHandle -> BadFileDescriptor("Bad file handle")
    is GetVolumeInformationError.OtherError -> InvalidArgument("Other error: $this")
}
