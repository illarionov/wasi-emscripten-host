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
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.MkdirError
import at.released.weh.filesystem.error.Mlink
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.ReadOnlyFileSystem
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.mkdir.Mkdir
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.windows.nativefunc.open.windowsNtCreateFileEx
import at.released.weh.filesystem.windows.win32api.close
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
        val path = pathResolver.getPath(input.baseDirectory, input.path)
            .getOrElse { return it.toResolveRelativePathErrors().left() }

        val createDisposition = if (input.failIfExists) {
            FILE_CREATE
        } else {
            FILE_OPEN_IF
        }

        return windowsNtCreateFileEx(
            path = path,
            withRootAccess = pathResolver.withRootAccess,
            desiredAccess = FILE_LIST_DIRECTORY or
                    FILE_READ_ATTRIBUTES or
                    FILE_TRAVERSE or
                    FILE_WRITE_ATTRIBUTES,
            fileAttributes = FILE_ATTRIBUTE_DIRECTORY,
            createDisposition = createDisposition,
            createOptions = FILE_DIRECTORY_FILE,
        )
            .mapLeft(::openFileToMkdirError)
            .flatMap { handle ->
                handle.close().mapLeft(::closeErrorToMkdirError)
            }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun openFileToMkdirError(error: OpenError): MkdirError = when (error) {
        is AccessDenied -> error
        is Again -> IoError(error.message)
        is BadFileDescriptor -> error
        is DiskQuota -> error
        is Exists -> error
        is Interrupted -> IoError(error.message)
        is InvalidArgument -> error
        is IoError -> error
        is Mfile -> IoError(error.message)
        is Mlink -> error
        is NameTooLong -> error
        is Nfile -> IoError(error.message)
        is NoEntry -> error
        is NoSpace -> error
        is NotCapable -> error
        is NotDirectory -> error
        is NotSupported -> InvalidArgument("Operation not supported")
        is Nxio -> IoError(error.message)
        is PathIsDirectory -> IoError(error.message)
        is PermissionDenied -> error
        is ReadOnlyFileSystem -> error
        is TextFileBusy -> IoError(error.message)
        is TooManySymbolicLinks -> error
    }

    private fun closeErrorToMkdirError(err: CloseError): MkdirError = when (err) {
        is BadFileDescriptor -> err
        is DiskQuota -> err
        is Interrupted -> IoError(err.message)
        is IoError -> err
        is NoSpace -> err
    }
}
