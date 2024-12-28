/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NOTHING_TO_INLINE")

package at.released.weh.filesystem.path

import arrow.core.Either
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.ACCES
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.IO
import at.released.weh.filesystem.model.FileSystemErrno.NOTCAPABLE
import at.released.weh.filesystem.model.FileSystemErrno.NOTDIR
import at.released.weh.filesystem.path.PathError.AbsolutePath
import at.released.weh.filesystem.path.PathError.AccessDenied
import at.released.weh.filesystem.path.PathError.EmptyPath
import at.released.weh.filesystem.path.PathError.FileDescriptorNotOpen
import at.released.weh.filesystem.path.PathError.InvalidPathFormat
import at.released.weh.filesystem.path.PathError.IoError
import at.released.weh.filesystem.path.PathError.NameTooLong
import at.released.weh.filesystem.path.PathError.NotDirectory
import at.released.weh.filesystem.path.PathError.OpenFileDescriptorLimitReached
import at.released.weh.filesystem.path.PathError.OtherOpenError
import at.released.weh.filesystem.path.PathError.PathOutsideOfRootPath
import at.released.weh.filesystem.path.PathError.TooManySymbolicLinks
import at.released.weh.filesystem.error.AccessDenied as FileSystemAccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor as FileSystemBadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument as FileSystemInvalidArgument
import at.released.weh.filesystem.error.IoError as FileSystemIoError
import at.released.weh.filesystem.error.NameTooLong as FileSystemNameTooLong
import at.released.weh.filesystem.error.Nfile as FileSystemNfile
import at.released.weh.filesystem.error.NotCapable as FileSystemNotCapable
import at.released.weh.filesystem.error.NotDirectory as FileSystemNotDirectory
import at.released.weh.filesystem.error.OpenError as FileSystemOpenError
import at.released.weh.filesystem.error.TooManySymbolicLinks as FileSystemTooManySymbolicLinks

public sealed interface PathError : FileSystemOperationError {
    public data class EmptyPath(
        override val message: String = "Path is empty",
        override val errno: FileSystemErrno = INVAL,
    ) : PathError, ResolvePathError

    public data class InvalidPathFormat(
        override val message: String,
        override val errno: FileSystemErrno = INVAL,
    ) : PathError, ResolvePathError

    public data class AbsolutePath(
        override val message: String,
        override val errno: FileSystemErrno = NOTCAPABLE,
    ) : PathError, ResolvePathError

    public data class PathOutsideOfRootPath(
        override val message: String,
        override val errno: FileSystemErrno = NOTCAPABLE,
    ) : PathError, ResolvePathError

    public data class AccessDenied(
        override val message: String,
        override val errno: FileSystemErrno = ACCES,
    ) : ResolvePathError

    public data class NotDirectory(
        override val message: String,
        override val errno: FileSystemErrno = NOTDIR,
    ) : ResolvePathError

    public data class FileDescriptorNotOpen(
        override val message: String,
        override val errno: FileSystemErrno = BADF,
    ) : ResolvePathError

    public data class NameTooLong(
        override val message: String,
        override val errno: FileSystemErrno = FileSystemErrno.NAMETOOLONG,
    ) : ResolvePathError

    public data class OpenFileDescriptorLimitReached(
        override val message: String,
        override val errno: FileSystemErrno = FileSystemErrno.NFILE,
    ) : ResolvePathError

    public data class TooManySymbolicLinks(
        override val message: String,
        override val errno: FileSystemErrno = FileSystemErrno.LOOP,
    ) : ResolvePathError

    public data class IoError(
        override val message: String,
        override val errno: FileSystemErrno = IO,
    ) : ResolvePathError

    public data class OtherOpenError(
        val openError: FileSystemOpenError,
        override val message: String = openError.message,
        override val errno: FileSystemErrno = openError.errno,
    ) : ResolvePathError
}

internal sealed interface ResolvePathError : FileSystemOperationError

internal fun PathError.toResolvePathError(): ResolvePathError = this as ResolvePathError

internal inline fun <T> Either<PathError, T>.withResolvePathError(): Either<ResolvePathError, T> =
    mapLeft(PathError::toResolvePathError)

internal inline fun <T> Either<PathError, T>.withPathErrorAsCommonError(): Either<ResolveRelativePathErrors, T> =
    mapLeft(PathError::toResolveRelativePathErrors)

@Suppress("MaxLineLength")
internal inline fun <T> Either<ResolvePathError, T>.withResolvePathErrorAsCommonError(): Either<ResolveRelativePathErrors, T> =
    mapLeft(ResolvePathError::toResolveRelativePathErrors)

internal fun PathError.toResolveRelativePathErrors(): ResolveRelativePathErrors = when (this) {
    is EmptyPath -> FileSystemInvalidArgument(this.message)
    is InvalidPathFormat -> FileSystemInvalidArgument(this.message)
    is AbsolutePath -> FileSystemInvalidArgument(this.message)
    is PathOutsideOfRootPath -> FileSystemNotCapable(this.message)
}

internal fun ResolvePathError.toResolveRelativePathErrors(): ResolveRelativePathErrors = when (this) {
    is EmptyPath -> FileSystemInvalidArgument(message)
    is InvalidPathFormat -> FileSystemInvalidArgument(message)
    is FileDescriptorNotOpen -> FileSystemBadFileDescriptor(message)
    is NotDirectory -> FileSystemNotDirectory(message)
    is AbsolutePath -> FileSystemNotCapable(message)
    is PathOutsideOfRootPath -> FileSystemNotCapable(message)
    is IoError -> FileSystemIoError(message)
    is OtherOpenError -> FileSystemBadFileDescriptor(message)
    is AccessDenied -> FileSystemAccessDenied(message)
    is NameTooLong -> FileSystemNameTooLong(message)
    is OpenFileDescriptorLimitReached -> FileSystemNfile(message)
    is TooManySymbolicLinks -> FileSystemTooManySymbolicLinks(message)
}

internal fun ResolvePathError.toGetCwdError(): GetCurrentWorkingDirectoryError = when (this) {
    is AbsolutePath -> FileSystemInvalidArgument("Path is absolute")
    is EmptyPath -> FileSystemInvalidArgument("Path is empty")
    is InvalidPathFormat -> FileSystemInvalidArgument("Invalid path format")
    is PathOutsideOfRootPath -> FileSystemAccessDenied("Path outside of root")
    is FileDescriptorNotOpen -> FileSystemAccessDenied("Invalid handle")
    is IoError -> FileSystemInvalidArgument("Can not read current directory")
    is OtherOpenError -> FileSystemAccessDenied("Failed to resolve path")
    is NotDirectory -> FileSystemAccessDenied("Not a directory")
    is AccessDenied -> FileSystemAccessDenied(message)
    is NameTooLong -> FileSystemAccessDenied(message)
    is OpenFileDescriptorLimitReached -> FileSystemAccessDenied(message)
    is TooManySymbolicLinks -> FileSystemAccessDenied(message)
}

internal fun ResolvePathError.toOpenError(): OpenError = when (this) {
    is AbsolutePath -> FileSystemNotCapable(message)
    is AccessDenied -> FileSystemAccessDenied(message)
    is EmptyPath -> FileSystemInvalidArgument(message)
    is FileDescriptorNotOpen -> FileSystemBadFileDescriptor(message)
    is InvalidPathFormat -> FileSystemInvalidArgument(message)
    is IoError -> FileSystemIoError(message)
    is NameTooLong -> FileSystemNameTooLong(message)
    is NotDirectory -> FileSystemNotDirectory(message)
    is OpenFileDescriptorLimitReached -> FileSystemNfile(message)
    is OtherOpenError -> openError
    is PathOutsideOfRootPath -> FileSystemNotCapable(message)
    is TooManySymbolicLinks -> FileSystemTooManySymbolicLinks(message)
}
