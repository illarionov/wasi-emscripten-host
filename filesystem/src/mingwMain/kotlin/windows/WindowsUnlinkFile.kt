/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
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
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_WRITE_DELETE
import at.released.weh.filesystem.windows.nativefunc.open.executeWithOpenFileHandle
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.fileinfo.FileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileBasicInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileDispositionInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileDispositionInfoEx
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.FILE_ATTRIBUTE_READONLY
import platform.windows.HANDLE

internal class WindowsUnlinkFile(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<UnlinkFile, UnlinkError, Unit> {
    override fun invoke(input: UnlinkFile): Either<UnlinkError, Unit> {
        return fsState.executeWithOpenFileHandle(
            baseDirectory = input.baseDirectory,
            path = input.path,
            followSymlinks = false,
            access = READ_WRITE_DELETE,
            errorMapper = ::openErrorToUnlinkError,
            block = ::deleteFileByHandle,
        )
    }

    internal companion object {
        private fun deleteFileByHandle(
            handle: HANDLE,
        ): Either<UnlinkError, Unit> = either {
            val info = handle.getFileAttributeTagInfo()
                .mapLeft(::statErrorToUnlinkError)
                .bind()

            // UnlinkFile can be used for both files and symlinks of any type
            if (info.fileAttributes.isDirectory && !info.fileAttributes.isSymlinkOrReparsePoint) {
                raise(PathIsDirectory("Path is a directory"))
            }

            return handle.setFileDispositionInfoEx(true)
                .swap()
                .flatMap { dispositionInfoExError ->
                    if (dispositionInfoExError.code.toInt() == ERROR_INVALID_PARAMETER) {
                        deleteWithSetFileDispositionInfo(handle, info).swap()
                    } else {
                        fileDispositionErrorToUnlinkError(dispositionInfoExError).right()
                    }
                }
                .swap()
        }

        private fun deleteWithSetFileDispositionInfo(
            handle: HANDLE,
            info: FileAttributeTagInfo,
        ): Either<UnlinkError, Unit> = either {
            if (info.fileAttributes.isReadOnly) {
                handle.setFileBasicInfo(
                    fileAttributes = FileAttributes(
                        info.fileAttributes.mask and FILE_ATTRIBUTE_READONLY.toUInt().inv(),
                    ),
                ).mapLeft(::setTimestampErrorToUnlinkError).bind()
            }
            return handle.setFileDispositionInfo(true).mapLeft(::fileDispositionErrorToUnlinkError)
        }

        internal fun statErrorToUnlinkError(error: StatError): UnlinkError = if (error is UnlinkError) {
            error
        } else {
            IoError(error.message)
        }

        private fun setTimestampErrorToUnlinkError(error: SetTimestampError): UnlinkError = if (error is UnlinkError) {
            error
        } else {
            IoError("Can not strip read-only attribute: ${error.message}")
        }

        @Suppress("CyclomaticComplexMethod")
        internal fun openErrorToUnlinkError(error: OpenError): UnlinkError = when (error) {
            is AccessDenied -> error
            is Again -> IoError(error.message)
            is BadFileDescriptor -> error
            is DiskQuota -> IoError(error.message)
            is Exists -> IoError(error.message)
            is Interrupted -> IoError(error.message)
            is InvalidArgument -> error
            is IoError -> error
            is Mfile -> IoError(error.message)
            is Mlink -> IoError(error.message)
            is NameTooLong -> error
            is Nfile -> IoError(error.message)
            is NoEntry -> error
            is NoSpace -> error
            is NotCapable -> error
            is NotDirectory -> error
            is NotSupported -> IoError(error.message)
            is Nxio -> IoError(error.message)
            is PathIsDirectory -> error
            is PermissionDenied -> error
            is ReadOnlyFileSystem -> error
            is TextFileBusy -> error
            is TooManySymbolicLinks -> error
        }

        internal fun fileDispositionErrorToUnlinkError(win32Code: Win32ErrorCode): UnlinkError =
            when (win32Code.code.toInt()) {
                // TODO: find error codes
                ERROR_ACCESS_DENIED -> AccessDenied("Cannot delete file, access denied")
                ERROR_FILE_NOT_FOUND -> NoEntry("File not found")
                ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect path")
                ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
                else -> InvalidArgument("Other error `$win32Code`")
            }
    }
}
