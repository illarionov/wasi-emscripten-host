/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path

import arrow.core.Either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.IO
import at.released.weh.filesystem.model.FileSystemErrno.NOTCAPABLE
import at.released.weh.filesystem.model.FileSystemErrno.NOTDIR

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
}

internal sealed interface ResolvePathError : FileSystemOperationError

internal fun PathError.toResolvePathError(): ResolvePathError = this as ResolvePathError

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> Either<PathError, T>.withResolvePathError(): Either<ResolvePathError, T> =
    mapLeft(PathError::toResolvePathError)

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> Either<PathError, T>.withPathErrorAsCommonError(): Either<ResolveRelativePathErrors, T> =
    mapLeft(PathError::toResolveRelativePathErrors)

@Suppress("NOTHING_TO_INLINE", "MaxLineLength")
internal inline fun <T> Either<ResolvePathError, T>.withResolvePathErrorAsCommonError(): Either<ResolveRelativePathErrors, T> =
    mapLeft(ResolvePathError::toResolveRelativePathErrors)

internal fun PathError.toResolveRelativePathErrors(): ResolveRelativePathErrors = when (this) {
    is PathError.EmptyPath -> InvalidArgument(this.message)
    is PathError.InvalidPathFormat -> InvalidArgument(this.message)
    is PathError.AbsolutePath -> InvalidArgument(this.message)
    is PathError.PathOutsideOfRootPath -> NotCapable(this.message)
}

internal fun ResolvePathError.toResolveRelativePathErrors(): ResolveRelativePathErrors = when (this) {
    is PathError.EmptyPath -> InvalidArgument(message)
    is PathError.InvalidPathFormat -> InvalidArgument(message)
    is PathError.FileDescriptorNotOpen -> BadFileDescriptor(message)
    is PathError.NotDirectory -> NotDirectory(message)
    is PathError.AbsolutePath -> NotCapable(message)
    is PathError.PathOutsideOfRootPath -> NotCapable(message)
    is PathError.IoError -> BadFileDescriptor(message)
}

internal fun ResolvePathError.toGetCwdError(): GetCurrentWorkingDirectoryError = when (this) {
    is PathError.AbsolutePath -> InvalidArgument("Path is absolute")
    is PathError.EmptyPath -> InvalidArgument("Path is empty")
    is PathError.InvalidPathFormat -> InvalidArgument("Invalid path format")
    is PathError.PathOutsideOfRootPath -> AccessDenied("Path outside of root")
    is PathError.FileDescriptorNotOpen -> AccessDenied("Invalid handle")
    is PathError.IoError -> InvalidArgument("Can not read current directory")
    is PathError.NotDirectory -> AccessDenied("Not a directory")
}
