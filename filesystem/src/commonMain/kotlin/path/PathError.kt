/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path

import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
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
import at.released.weh.filesystem.model.FileSystemErrno.PERM

public sealed interface PathError : FileSystemOperationError {
    public class EmptyPath(
        override val message: String,
        override val errno: FileSystemErrno = INVAL,
    ) : PathError, ResolvePathError

    public class InvalidPathFormat(
        override val message: String,
        override val errno: FileSystemErrno = INVAL,
    ) : PathError, ResolvePathError

    public class AbsolutePath(
        override val message: String,
        override val errno: FileSystemErrno = PERM,
    ) : ResolvePathError

    public class NotDirectory(
        override val message: String,
        override val errno: FileSystemErrno = NOTDIR,
    ) : ResolvePathError

    public class FileDescriptorNotOpen(
        override val message: String,
        override val errno: FileSystemErrno = BADF,
    ) : ResolvePathError

    public class IoError(
        override val message: String,
        override val errno: FileSystemErrno = IO,
    ) : ResolvePathError

    public class PathOutsideOfRootPath(
        override val message: String,
        override val errno: FileSystemErrno = NOTCAPABLE,
    ) : ResolvePathError
}

internal sealed interface ResolvePathError : FileSystemOperationError

internal fun PathError.toCommonError(): ResolveRelativePathErrors = when (this) {
    is PathError.EmptyPath -> InvalidArgument(this.message)
    is PathError.InvalidPathFormat -> InvalidArgument(this.message)
}

internal fun ResolvePathError.toCommonError(): ResolveRelativePathErrors = when (this) {
    is PathError.EmptyPath -> InvalidArgument(message)
    is PathError.InvalidPathFormat -> InvalidArgument(message)
    is PathError.FileDescriptorNotOpen -> BadFileDescriptor(message)
    is PathError.NotDirectory -> NotDirectory(message)
    is PathError.AbsolutePath -> NotCapable(message)
    is PathError.PathOutsideOfRootPath -> NotCapable(message)
    is PathError.IoError -> BadFileDescriptor(message)
}
