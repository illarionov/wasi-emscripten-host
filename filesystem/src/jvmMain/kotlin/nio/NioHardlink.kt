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
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.PermissionDenied
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.fdresource.nio.createSymlink
import at.released.weh.filesystem.fdresource.nio.readSymbolicLink
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.hardlink.Hardlink
import at.released.weh.filesystem.path.virtual.VirtualPath
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.io.path.isDirectory
import kotlin.io.path.isSymbolicLink

internal class NioHardlink(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Hardlink, HardlinkError, Unit> {
    private val createLinkFollowSymlinks: Boolean by lazy(PUBLICATION) {
        val osName = System.getProperty("os.name") ?: "generic"
        val isLinux = osName.findAnyOf(listOf("nix", "nux", "aix"), ignoreCase = true) != null
        !isLinux
    }

    override fun invoke(input: Hardlink): Either<HardlinkError, Unit> = either {
        val oldVirtualPath = VirtualPath.of(input.oldPath).mapLeft { InvalidArgument(it.message) }.bind()

        val oldPath = fsState.pathResolver.resolve(
            path = oldVirtualPath,
            baseDirectory = input.oldBaseDirectory,
            allowEmptyPath = false,
            followSymlinks = input.followSymlinks,
        ).mapLeft(ResolvePathError::toCommonError).bind()

        val newVirtualPath = VirtualPath.of(input.newPath).mapLeft { InvalidArgument(it.message) }.bind()
        val newPath = fsState.pathResolver.resolve(
            path = newVirtualPath,
            baseDirectory = input.newBaseDirectory,
            allowEmptyPath = false,
            followSymlinks = false,
        ).mapLeft(ResolvePathError::toCommonError).bind()

        if (!input.followSymlinks && createLinkFollowSymlinks && oldPath.isSymbolicLink()) {
            copySymlink(oldPath, newPath).bind()
        } else {
            createHardlink(oldPath, newPath).bind()
        }
    }

    private fun createHardlink(
        oldPath: Path,
        newPath: Path,
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
                    if (otherFile != null && Path.of(otherFile).isDirectory()) {
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
        oldSymlink: Path,
        newPath: Path,
    ): Either<HardlinkError, Unit> = either {
        val target: String = readSymbolicLink(oldSymlink)
            .mapLeft { it.toHardlinkError() }
            .bind()
        createSymlink(newPath, target, true)
            .mapLeft { it.toHardlinkError() }
    }

    private fun ReadLinkError.toHardlinkError(): HardlinkError = this as HardlinkError

    private fun SymlinkError.toHardlinkError(): HardlinkError = this as HardlinkError
}
