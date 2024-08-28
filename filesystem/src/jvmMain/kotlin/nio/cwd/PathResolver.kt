/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.cwd

import arrow.core.Either
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Errno.BADF
import at.released.weh.filesystem.model.Errno.INVAL
import at.released.weh.filesystem.model.Errno.NOENT
import at.released.weh.filesystem.model.Errno.NOTDIR
import java.nio.file.Path

internal interface PathResolver {
    fun resolve(
        path: String?,
        baseDirectory: BaseDirectory,
        allowEmptyPath: Boolean = false,
        followSymlinks: Boolean = true,
    ): Either<ResolvePathError, Path>

    sealed interface ResolvePathError : FileSystemOperationError {
        class RelativePath(
            override val message: String,
            override val errno: Errno = INVAL,
        ) : ResolvePathError

        class InvalidPath(
            override val message: String,
            override val errno: Errno = INVAL,
        ) : ResolvePathError

        class NotDirectory(
            override val message: String,
            override val errno: Errno = NOTDIR,
        ) : ResolvePathError

        class FileDescriptorNotOpen(
            override val message: String,
            override val errno: Errno = BADF,
        ) : ResolvePathError

        class EmptyPath(
            override val message: String,
            override val errno: Errno = NOENT,
        ) : ResolvePathError
    }
}
