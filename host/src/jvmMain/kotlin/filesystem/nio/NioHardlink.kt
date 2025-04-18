/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.Exists
import at.released.weh.filesystem.error.HardlinkError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.ext.Os
import at.released.weh.filesystem.fdresource.nio.createSymlink
import at.released.weh.filesystem.fdresource.nio.readSymbolicLink
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.hardlink.Hardlink
import at.released.weh.filesystem.path.withResolvePathErrorAsCommonError
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystemException
import java.nio.file.Files
import kotlin.io.path.isDirectory
import kotlin.io.path.isSymbolicLink
import java.nio.file.Path as NioPath

internal class NioHardlink(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Hardlink, HardlinkError, Unit> {
    // Workaround for inconsistent behavior of Files.createLink across operating systems.
    // See [JDK-8343823](https://bugs.openjdk.org/browse/JDK-8344633).
    private val createLinkFollowSymlinks: Boolean = with(Os) { !isLinux && !isWindows }
    override fun invoke(input: Hardlink): Either<HardlinkError, Unit> = either {
        val oldPath = fsState.pathResolver.resolve(
            path = input.oldPath,
            baseDirectory = input.oldBaseDirectory,
            followSymlinks = input.followSymlinks,
        ).withResolvePathErrorAsCommonError().bind()

        val newPath = fsState.pathResolver.resolve(
            path = input.newPath,
            baseDirectory = input.newBaseDirectory,
            followSymlinks = false,
        ).withResolvePathErrorAsCommonError().bind()

        if (!input.followSymlinks && createLinkFollowSymlinks && oldPath.nio.isSymbolicLink()) {
            copySymlink(oldPath.nio, newPath.nio).bind()
        } else {
            createHardlink(oldPath.nio, newPath.nio).bind()
        }
    }
}

private fun createHardlink(
    oldPath: NioPath,
    newPath: NioPath,
): Either<HardlinkError, Unit> {
    return Either.catch {
        Files.createLink(newPath, oldPath)
        Unit
    }.mapLeft { throwable ->
        when (throwable) {
            is UnsupportedOperationException -> PermissionDenied("Filesystem does not support hardlinks")
            is FileAlreadyExistsException -> Exists("Link path already exists")
            is FileSystemException -> {
                val otherFile = throwable.otherFile
                if (otherFile != null && NioPath.of(otherFile).isDirectory()) {
                    PermissionDenied("Can not create hardlink to directory")
                } else {
                    IoError("Filesystem exception `${throwable.message}`")
                }
            }

            is IOException -> IoError("I/o exception `${throwable.message}`")
            else -> IoError("Other error `${throwable.message}`")
        }
    }
}

private fun copySymlink(
    oldSymlink: NioPath,
    newPath: NioPath,
): Either<HardlinkError, Unit> = either {
    val target: NioPath = readSymbolicLink(oldSymlink)
        .mapLeft { it.toHardlinkError() }
        .bind()
    createSymlink(newPath, target, true).mapLeft { it.toHardlinkError() }
}

private fun ReadLinkError.toHardlinkError(): HardlinkError = this as HardlinkError

private fun SymlinkError.toHardlinkError(): HardlinkError = this as HardlinkError
