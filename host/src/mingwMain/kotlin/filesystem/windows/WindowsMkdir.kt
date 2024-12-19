/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.MkdirError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.NtCreateFileResult
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtCreateFile
import at.released.weh.filesystem.windows.win32api.errorcode.NtStatus
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_CREATE
import platform.windows.FILE_DIRECTORY_FILE
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_OPEN_IF
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_TRAVERSE
import platform.windows.FILE_WRITE_ATTRIBUTES

internal class WindowsMkdir(
    private val pathResolver: WindowsPathResolver,
) : FileSystemOperationHandler<Mkdir, MkdirError, Unit> {
    override fun invoke(input: Mkdir): Either<MkdirError, Unit> = either {
        val ntPath = pathResolver.getNtPath(input.baseDirectory, input.path)
            .getOrElse { return it.toResolveRelativePathErrors().left() }

        val createDisposition = if (input.failIfExists) {
            FILE_CREATE
        } else {
            FILE_OPEN_IF
        }

        return windowsNtCreateFile(
            ntPath = ntPath,
            desiredAccess = FILE_LIST_DIRECTORY or
                    FILE_READ_ATTRIBUTES or
                    FILE_TRAVERSE or
                    FILE_WRITE_ATTRIBUTES,
            fileAttributes = FILE_ATTRIBUTE_DIRECTORY,
            createDisposition = createDisposition,
            createOptions = FILE_DIRECTORY_FILE,
        )
            .mapLeft(::ntCreateFileResultToMkdirError)
            .flatMap { handle ->
                handle.close().mapLeft(::closeErrorToMkdirError)
            }
    }

    private fun ntCreateFileResultToMkdirError(result: NtCreateFileResult): MkdirError = when (result.status.raw) {
        // XXX: find more possible error codes
        NtStatus.STATUS_INVALID_PARAMETER -> InvalidArgument("NtCreateFile failed: invalid argument")
        NtStatus.STATUS_UNSUCCESSFUL -> IoError("Other error ${result.status}")
        NtStatus.STATUS_ACCESS_DENIED -> AccessDenied("Access denied")
        NtStatus.STATUS_NOT_A_DIRECTORY -> NotDirectory("Not a directory")
        NtStatus.STATUS_NOT_IMPLEMENTED -> InvalidArgument("Operation not supported")
        NtStatus.STATUS_OBJECT_NAME_INVALID -> InvalidArgument("Invalid filename")
        NtStatus.STATUS_OBJECT_NAME_NOT_FOUND -> NoEntry("Name not found")
        NtStatus.STATUS_OBJECT_PATH_NOT_FOUND -> NoEntry("Path not found")
        NtStatus.STATUS_OBJECT_NAME_COLLISION -> Exists("Directory exists")
        NtStatus.STATUS_OBJECT_PATH_SYNTAX_BAD -> InvalidArgument("Unsupported path format")
        else -> IoError("Other error ${result.status}")
    }

    private fun closeErrorToMkdirError(err: CloseError): MkdirError = when (err) {
        is BadFileDescriptor -> err
        is DiskQuota -> err
        is Interrupted -> IoError(err.message)
        is IoError -> err
        is NoSpace -> err
    }
}
