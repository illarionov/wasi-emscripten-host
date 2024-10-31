/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.symlink.Symlink
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path

internal class NioSymlink(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Symlink, SymlinkError, Unit> {
    override fun invoke(input: Symlink): Either<SymlinkError, Unit> {
        return fsState.executeWithPath(
            input.newPathBaseDirectory,
            input.newPath,
        ) { resolvedPath: Either<ResolvePathError, Path> ->
            resolvedPath
                .mapLeft(ResolvePathError::toCommonError)
                .flatMap {
                    createSymlink(it, input.oldPath, input.allowAbsoluteOldPath)
                }
        }
    }

    private fun createSymlink(
        linkpath: Path,
        target: String,
        allowAbsoluteOldPath: Boolean,
    ): Either<SymlinkError, Unit> {
        return validateSymlinkTarget(target, allowAbsoluteOldPath)
            .flatMap {
                val targetPath = Path.of(target)
                createSymlink(linkpath, targetPath)
            }
    }

    private fun validateSymlinkTarget(
        target: String,
        allowAbsolutePath: Boolean,
    ): Either<SymlinkError, Unit> = either {
        val cleanedTarget = target.trim()
        if (!allowAbsolutePath && Path.of(cleanedTarget).isAbsolute) {
            raise(InvalidArgument("link destination should be relative"))
        }
    }

    private fun createSymlink(
        linkpath: Path,
        target: Path,
    ): Either<SymlinkError, Unit> {
        return Either.catch {
            Files.createSymbolicLink(linkpath, target)
            Unit
        }.mapLeft {
            when (it) {
                is UnsupportedOperationException -> PermissionDenied("Filesystem does not support symbolic links")
                is FileAlreadyExistsException -> Exists("Link path already exists")
                is IOException -> IoError("I/o exception `${it.message}`")
                else -> IoError("Other error `${it.message}`")
            }
        }
    }
}
