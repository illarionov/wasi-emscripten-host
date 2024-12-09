/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.DirectoryNotEmpty
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.RenameError
import at.released.weh.filesystem.fdresource.NioFdResource
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.rename.Rename
import at.released.weh.filesystem.path.virtual.VirtualPath
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import kotlin.concurrent.withLock
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

internal class NioRename(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Rename, RenameError, Unit> {
    override fun invoke(input: Rename): Either<RenameError, Unit> = either {
        val oldPath: Path
        val newPath: Path
        val oldFdResource: NioFdResource?
        val newFdResource: NioFdResource?

        val oldVirtualPath = VirtualPath.of(input.oldPath).mapLeft { InvalidArgument(it.message) }.bind()
        val newVirtualPath = VirtualPath.of(input.newPath).mapLeft { InvalidArgument(it.message) }.bind()

        fsState.fdsLock.withLock {
            oldPath = fsState.pathResolver.resolve(
                path = oldVirtualPath,
                baseDirectory = input.oldBaseDirectory,
                allowEmptyPath = true,
                followSymlinks = false,
            )
                .mapLeft(ResolvePathError::toCommonError)
                .bind()

            newPath = fsState.pathResolver.resolve(
                path = newVirtualPath,
                baseDirectory = input.newBaseDirectory,
                allowEmptyPath = true,
                followSymlinks = false,
            )
                .mapLeft(ResolvePathError::toCommonError)
                .bind()

            oldFdResource = fsState.findUnsafe(oldPath)
            newFdResource = fsState.findUnsafe(newPath)
        }

        if (newFdResource != null) {
            raise(PermissionDenied("Can not rename to opened file or directory"))
        }

        rename(oldPath, newPath).bind()

        oldFdResource?.updatePath(newPath, newVirtualPath)
    }

    private fun rename(
        oldPath: Path,
        newPath: Path,
    ): Either<RenameError, Path> {
        return when {
            oldPath.isDirectory() && newPath.exists() && !newPath.isDirectory() ->
                NotDirectory("Can not rename directory to non-directory").left()

            oldPath.exists() && !oldPath.isDirectory() && newPath.isDirectory() ->
                PathIsDirectory("Can not rename non-directory to directory").left()

            else -> Either.catch {
                Files.move(oldPath, newPath, REPLACE_EXISTING)
            }.mapLeft {
                when (it) {
                    is UnsupportedOperationException -> PermissionDenied("Unsupported copy option")
                    is FileAlreadyExistsException -> Exists("Destination path already exists")
                    is DirectoryNotEmptyException -> DirectoryNotEmpty("Moved directory is not empty")
                    is IOException -> IoError("I/o exception `${it.message}`")
                    else -> IoError("Other error `${it.message}`")
                }
            }
        }
    }
}
