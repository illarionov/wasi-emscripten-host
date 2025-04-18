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
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.RenameError
import at.released.weh.filesystem.fdresource.NioFdResource
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.rename.Rename
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.withResolvePathErrorAsCommonError
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
        val oldPath: NioRealPath
        val newPath: NioRealPath
        val oldFdResource: NioFdResource?
        val newFdResource: NioFdResource?

        fsState.fdsLock.withLock {
            oldPath = fsState.pathResolver.resolve(
                path = input.oldPath,
                baseDirectory = input.oldBaseDirectory,
                followSymlinks = false,
            ).withResolvePathErrorAsCommonError().bind()

            newPath = fsState.pathResolver.resolve(
                path = input.newPath,
                baseDirectory = input.newBaseDirectory,
                followSymlinks = false,
            ).withResolvePathErrorAsCommonError().bind()

            oldFdResource = fsState.findUnsafe(oldPath)
            newFdResource = fsState.findUnsafe(newPath)
        }

        if (newFdResource != null) {
            raise(PermissionDenied("Can not rename to opened file or directory"))
        }

        rename(oldPath, newPath).bind()

        oldFdResource?.updatePath(newPath, input.newPath)
    }

    private fun rename(
        oldPath: NioRealPath,
        newPath: NioRealPath,
    ): Either<RenameError, Path> {
        val oldNioPath = oldPath.nio
        val newNioPath = newPath.nio

        return when {
            oldNioPath.isDirectory() && newNioPath.exists() && !newNioPath.isDirectory() ->
                NotDirectory("Can not rename directory to non-directory").left()

            oldNioPath.exists() && !oldNioPath.isDirectory() && newNioPath.isDirectory() ->
                PathIsDirectory("Can not rename non-directory to directory").left()

            else -> Either.catch {
                Files.move(oldNioPath, newNioPath, REPLACE_EXISTING)
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
