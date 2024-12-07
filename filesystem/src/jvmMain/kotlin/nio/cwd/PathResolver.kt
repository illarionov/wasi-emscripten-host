/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.cwd

import arrow.core.Either
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.NOENT
import at.released.weh.filesystem.model.FileSystemErrno.NOTCAPABLE
import at.released.weh.filesystem.model.FileSystemErrno.NOTDIR
import at.released.weh.filesystem.model.FileSystemErrno.PERM
import at.released.weh.filesystem.path.virtual.VirtualPath
import java.nio.file.Path

internal interface PathResolver {
    fun resolve(
        path: VirtualPath?,
        baseDirectory: BaseDirectory,
        allowEmptyPath: Boolean = false,
        followSymlinks: Boolean = true,
    ): Either<ResolvePathError, Path>

    sealed interface ResolvePathError : FileSystemOperationError {
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
}
