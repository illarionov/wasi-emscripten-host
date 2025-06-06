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
import at.released.weh.filesystem.error.DirectoryNotEmpty
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
import at.released.weh.filesystem.error.RenameError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TextFileBusy
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.rename.Rename
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.windows.WindowsRename.DestinationFileType.Directory
import at.released.weh.filesystem.windows.WindowsRename.DestinationFileType.File
import at.released.weh.filesystem.windows.WindowsRename.DestinationFileType.NotExists
import at.released.weh.filesystem.windows.WindowsRename.DestinationFileType.SymlinkToDirectory
import at.released.weh.filesystem.windows.WindowsRename.DestinationFileType.SymlinkToFile
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_WRITE_DELETE
import at.released.weh.filesystem.windows.nativefunc.open.windowsOpenForAttributeAccess
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.errorcode.Win32ErrorCode
import at.released.weh.filesystem.windows.win32api.fileinfo.FileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileBasicInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileDispositionInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.setFileRenameInfo
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import at.released.weh.filesystem.windows.win32api.filepath.toResolveRelativePathError
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import platform.windows.ERROR_ACCESS_DENIED
import platform.windows.ERROR_ALREADY_EXISTS
import platform.windows.ERROR_FILE_NOT_FOUND
import platform.windows.ERROR_INVALID_HANDLE
import platform.windows.ERROR_INVALID_PARAMETER
import platform.windows.FILE_ATTRIBUTE_READONLY
import platform.windows.HANDLE
import platform.windows.PathIsDirectoryEmptyW

internal class WindowsRename(
    private val pathResolver: WindowsPathResolver,
) : FileSystemOperationHandler<Rename, RenameError, Unit> {
    override fun invoke(input: Rename): Either<RenameError, Unit> = either {
        return pathResolver.executeWithOpenFileHandle(
            baseDirectory = input.oldBaseDirectory,
            path = input.oldPath,
            followSymlinks = false,
            access = READ_WRITE_DELETE,
            errorMapper = ::openErrorToRenameError,
        ) { oldHandle ->
            renameByFileHandle(oldHandle, input.newBaseDirectory, input.newPath)
        }
    }

    private fun renameByFileHandle(
        handle: HANDLE,
        newBaseDirectory: BaseDirectory,
        newPath: VirtualPath,
    ): Either<RenameError, Unit> = either {
        val (destinationPath, destinationType, dstHandle) = DestinationInfoReader(
            pathResolver,
            newBaseDirectory,
            newPath,
        ).read().bind()
        return when (destinationType) {
            NotExists -> {
                dstHandle?.close()?.onLeft { /* ignore */ }
                doRename(handle, destinationPath)
            }

            else -> replaceExistingFile(handle, destinationPath, destinationType, dstHandle)
        }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun replaceExistingFile(
        handle: HANDLE,
        destinationPath: WindowsRealPath,
        destinationType: DestinationFileType,
        dstHandle: HANDLE?,
    ): Either<RenameError, Unit> {
        var dstHandleClosed = false
        return either<RenameError, Unit> {
            val sourceType = handle.getFileAttributeTagInfo()
                .mapLeft(::statErrorToRenameError)
                .bind()
            when (destinationType) {
                NotExists -> error("Should not be called")
                is File -> {
                    if (sourceType.fileAttributes.isDirectory || sourceType.isSymlink) {
                        raise(NotDirectory("Can not rename directory to non-directory"))
                    }
                    if (destinationType.attributes.isReadOnly) {
                        dstHandle?.setFileBasicInfo(
                            fileAttributes = FileAttributes(
                                destinationType.attributes.mask and FILE_ATTRIBUTE_READONLY.toUInt().inv(),
                            ),
                        )
                            ?.mapLeft(::setTimestampErrorToRenameError)
                            ?.bind()
                    }
                }

                is Directory -> {
                    if (!sourceType.fileAttributes.isDirectory || sourceType.isSymlink) {
                        raise(PathIsDirectory("Can not rename non-directory to directory"))
                    }
                    if (!destinationType.isEmpty) {
                        raise(DirectoryNotEmpty("Can not overwrite non-empty directory"))
                    }
                    dstHandle?.setFileDispositionInfo(true)
                        ?.mapLeft(::setFileErrorToRenameError)
                        ?.bind()
                }

                SymlinkToDirectory -> if (!sourceType.fileAttributes.isDirectory || !sourceType.isSymlink) {
                    raise(NotDirectory("Can not rename non-symlink-to-directory to symlink-to-directory"))
                }

                SymlinkToFile -> if (sourceType.fileAttributes.isDirectory || !sourceType.isSymlink) {
                    raise(PathIsDirectory("Can not rename non-symlink-to-file to symlink-to-file"))
                }
            }

            if (!dstHandleClosed) {
                dstHandleClosed = true
                dstHandle?.close()?.onLeft { /* ignore */ }
            }

            doRename(handle, destinationPath).bind()
        }.also {
            if (!dstHandleClosed) {
                dstHandleClosed = true
                dstHandle?.close()?.onLeft { /* ignore */ }
            }
        }
    }

    private fun doRename(handle: HANDLE, newPath: WindowsRealPath): Either<RenameError, Unit> {
        return handle.setFileRenameInfo(newPath, true).mapLeft(::setFileErrorToRenameError)
    }

    private class DestinationInfoReader(
        private val pathResolver: WindowsPathResolver,
        private val newBaseDirectory: BaseDirectory,
        private val newPath: VirtualPath,
    ) {
        fun read(): Either<RenameError, DestinationPathInfo> {
            val newPath = pathResolver.getPath(newBaseDirectory, this@DestinationInfoReader.newPath)
                .getOrElse { return it.toResolveRelativePathErrors().left() }

            val newPathAttributesHandle = windowsOpenForAttributeAccess(
                path = newPath,
                followSymlinks = false,
                access = READ_WRITE_DELETE,
            ).getOrElse {
                return getInfoOnOpenError(it)
            }
            val info = getPathInfo(newPathAttributesHandle)
            return info
        }

        private fun getInfoOnOpenError(
            dstOpenError: OpenError,
        ): Either<RenameError, DestinationPathInfo> = if (dstOpenError is NoEntry) {
            pathResolver.getWindowsPath(newBaseDirectory, newPath)
                .map {
                    DestinationPathInfo(
                        resolvedRealPath = it,
                        type = NotExists,
                        dstHandle = null,
                    )
                }
        } else {
            openErrorToRenameError(dstOpenError).left()
        }

        private fun getPathInfo(handle: HANDLE): Either<RenameError, DestinationPathInfo> {
            return handle.getFileAttributeTagInfo()
                .mapLeft(::statErrorToRenameError)
                .flatMap { attrs ->
                    handle.getFinalPath()
                        .mapLeft(GetFinalPathError::toResolveRelativePathError)
                        .map { finalPath ->
                            getPathInfo(handle, finalPath, attrs)
                        }
                }
        }

        private fun getPathInfo(
            attributesHandle: HANDLE,
            finalPath: WindowsRealPath,
            fileAttributes: FileAttributeTagInfo,
        ): DestinationPathInfo {
            return when {
                fileAttributes.isSymlink -> DestinationPathInfo(
                    resolvedRealPath = finalPath,
                    type = if (fileAttributes.fileAttributes.isDirectory) SymlinkToDirectory else SymlinkToFile,
                    dstHandle = attributesHandle,
                )

                fileAttributes.fileAttributes.isDirectory -> {
                    val isEmpty = PathIsDirectoryEmptyW(finalPath.kString)
                    DestinationPathInfo(
                        resolvedRealPath = finalPath,
                        type = Directory(isEmpty = isEmpty != 0),
                        dstHandle = attributesHandle,
                    )
                }

                else -> DestinationPathInfo(
                    resolvedRealPath = finalPath,
                    type = File(fileAttributes.fileAttributes),
                    dstHandle = attributesHandle,
                )
            }
        }
    }

    private sealed class DestinationFileType {
        data object NotExists : DestinationFileType()
        data object SymlinkToFile : DestinationFileType()
        data object SymlinkToDirectory : DestinationFileType()
        data class File(val attributes: FileAttributes) : DestinationFileType()
        data class Directory(val isEmpty: Boolean) : DestinationFileType()
    }

    private data class DestinationPathInfo(
        val resolvedRealPath: WindowsRealPath,
        val type: DestinationFileType,
        val dstHandle: HANDLE?,
    )

    private companion object {
        private fun statErrorToRenameError(error: StatError): RenameError = if (error is RenameError) {
            error
        } else {
            IoError(error.message)
        }

        @Suppress("CyclomaticComplexMethod")
        private fun openErrorToRenameError(error: OpenError): RenameError = when (error) {
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
            is NotSupported -> IoError(error.message)
            is Nxio -> IoError(error.message)
            is PathIsDirectory -> error
            is PermissionDenied -> error
            is ReadOnlyFileSystem -> error
            is TextFileBusy -> IoError(error.message)
            is TooManySymbolicLinks -> error
        }

        private fun setFileErrorToRenameError(code: Win32ErrorCode): RenameError = when (code.code.toInt()) {
            // XXX: find error codes
            ERROR_ACCESS_DENIED -> AccessDenied("Cannot rename file or directory, access denied")
            ERROR_FILE_NOT_FOUND -> NoEntry("File not found")
            ERROR_ALREADY_EXISTS -> Exists("File or directory already exists")
            ERROR_INVALID_PARAMETER -> InvalidArgument("Incorrect path")
            ERROR_INVALID_HANDLE -> BadFileDescriptor("Bad file handle")
            else -> InvalidArgument("Other error `$this`")
        }

        private fun setTimestampErrorToRenameError(error: SetTimestampError): RenameError = if (error is RenameError) {
            error
        } else {
            IoError("Can not strip read-only attribute on file: ${error.message}")
        }
    }
}
