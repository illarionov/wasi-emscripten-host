/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.cwd

import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.NOENT
import at.released.weh.filesystem.model.FileSystemErrno.NOTCAPABLE
import at.released.weh.filesystem.model.FileSystemErrno.NOTDIR
import at.released.weh.filesystem.model.FileSystemErrno.PERM

internal sealed interface ResolvePathError : FileSystemOperationError {
    class AbsolutePath(
        override val message: String,
        override val errno: FileSystemErrno = PERM,
    ) : ResolvePathError

    class InvalidPath(
        override val message: String,
        override val errno: FileSystemErrno = INVAL,
    ) : ResolvePathError

    class NotDirectory(
        override val message: String,
        override val errno: FileSystemErrno = NOTDIR,
    ) : ResolvePathError

    class FileDescriptorNotOpen(
        override val message: String,
        override val errno: FileSystemErrno = BADF,
    ) : ResolvePathError

    class EmptyPath(
        override val message: String,
        override val errno: FileSystemErrno = NOENT,
    ) : ResolvePathError

    class PathOutsideOfRootPath(
        override val message: String,
        override val errno: FileSystemErrno = NOTCAPABLE,
    ) : ResolvePathError
}

internal fun ResolvePathError.toCommonError(): ResolveRelativePathErrors = when (this) {
    is ResolvePathError.EmptyPath -> InvalidArgument(message)
    is ResolvePathError.FileDescriptorNotOpen -> BadFileDescriptor(message)
    is ResolvePathError.InvalidPath -> InvalidArgument(message)
    is ResolvePathError.NotDirectory -> NotDirectory(message)
    is ResolvePathError.AbsolutePath -> NotCapable(message)
    is ResolvePathError.PathOutsideOfRootPath -> NotCapable(message)
}
