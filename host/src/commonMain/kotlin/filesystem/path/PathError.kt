/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("NOTHING_TO_INLINE")

package at.released.weh.filesystem.path

import arrow.core.Either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.IO
import at.released.weh.filesystem.model.FileSystemErrno.NOTCAPABLE
import at.released.weh.filesystem.model.FileSystemErrno.NOTDIR
import at.released.weh.filesystem.path.PathError.AbsolutePath
import at.released.weh.filesystem.path.PathError.EmptyPath
import at.released.weh.filesystem.path.PathError.FileDescriptorNotOpen
import at.released.weh.filesystem.path.PathError.InvalidPathFormat
import at.released.weh.filesystem.path.PathError.IoError
import at.released.weh.filesystem.path.PathError.OtherOpenError
import at.released.weh.filesystem.path.PathError.PathOutsideOfRootPath

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

    public data class NotDirectory(
        override val message: String,
        override val errno: FileSystemErrno = NOTDIR,
    ) : ResolvePathError

    public data class FileDescriptorNotOpen(
        override val message: String,
        override val errno: FileSystemErrno = BADF,
    ) : ResolvePathError

    public data class IoError(
        override val message: String,
        override val errno: FileSystemErrno = IO,
    ) : ResolvePathError

    public data class OtherOpenError(
        val openError: OpenError,
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
    is PathError.EmptyPath -> InvalidArgument(this.message)
    is PathError.InvalidPathFormat -> InvalidArgument(this.message)
    is PathError.AbsolutePath -> InvalidArgument(this.message)
    is PathError.PathOutsideOfRootPath -> NotCapable(this.message)
}

internal fun ResolvePathError.toResolveRelativePathErrors(): ResolveRelativePathErrors = when (this) {
    is EmptyPath -> InvalidArgument(message)
    is InvalidPathFormat -> InvalidArgument(message)
    is FileDescriptorNotOpen -> BadFileDescriptor(message)
    is PathError.NotDirectory -> NotDirectory(message)
    is AbsolutePath -> NotCapable(message)
    is PathOutsideOfRootPath -> NotCapable(message)
    is IoError -> BadFileDescriptor(message)
    is OtherOpenError -> BadFileDescriptor(message)
}

internal fun ResolvePathError.toGetCwdError(): GetCurrentWorkingDirectoryError = when (this) {
    is AbsolutePath -> InvalidArgument("Path is absolute")
    is EmptyPath -> InvalidArgument("Path is empty")
    is InvalidPathFormat -> InvalidArgument("Invalid path format")
    is PathOutsideOfRootPath -> AccessDenied("Path outside of root")
    is FileDescriptorNotOpen -> AccessDenied("Invalid handle")
    is IoError -> InvalidArgument("Can not read current directory")
    is OtherOpenError -> AccessDenied("Failed to resolve path")
    is PathError.NotDirectory -> AccessDenied("Not a directory")
}
