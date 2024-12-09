/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.DirectoryNotEmpty
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.JvmPathResolver
import at.released.weh.filesystem.nio.cwd.ResolvePathError
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import at.released.weh.filesystem.error.NotDirectory as BaseNotDirectory

internal class NioUnlinkFile(
    private val pathResolver: JvmPathResolver,
) : FileSystemOperationHandler<UnlinkFile, UnlinkError, Unit> {
    override fun invoke(input: UnlinkFile): Either<UnlinkError, Unit> = either {
        val path: Path = pathResolver.resolve(
            input.path,
            input.baseDirectory,
            followSymlinks = false,
        )
            .mapLeft { it.toUnlinkError() }
            .bind()

        if (path.isDirectory(NOFOLLOW_LINKS)) {
            raise(PathIsDirectory("`$path` is a directory"))
        } else if (input.path.isDirectoryRequest()) {
            raise(NotDirectory("Path with trailing slash"))
        }

        return Either.catch {
            Files.delete(path)
        }.mapLeft {
            it.toUnlinkError(path)
        }
    }

    companion object {
        internal fun ResolvePathError.toUnlinkError(): UnlinkError = when (this) {
            is ResolvePathError.EmptyPath -> NoEntry(message)
            is ResolvePathError.FileDescriptorNotOpen -> BadFileDescriptor(message)
            is ResolvePathError.InvalidPath -> BadFileDescriptor(message)
            is ResolvePathError.NotDirectory -> BaseNotDirectory(message)
            is ResolvePathError.AbsolutePath -> BadFileDescriptor(message)
            is ResolvePathError.PathOutsideOfRootPath -> NotCapable(message)
        }

        internal fun Throwable.toUnlinkError(path: Path): UnlinkError = when (this) {
            is NoSuchFileException -> NoEntry("No file `$path`")
            is DirectoryNotEmptyException -> DirectoryNotEmpty("Directory not empty")
            is IOException -> IoError("I/O Error: $message")
            else -> throw IllegalStateException("Unexpected error", this)
        }
    }
}
