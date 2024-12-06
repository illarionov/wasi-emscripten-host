/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.cwd

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.fdresource.NioDirectoryFdResource
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.nio.NioFileSystemState
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError.AbsolutePath
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError.EmptyPath
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError.FileDescriptorNotOpen
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError.InvalidPath
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError.NotDirectory
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError.PathOutsideOfRootPath
import at.released.weh.filesystem.preopened.VirtualPath
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString

@Suppress("ReturnCount")
internal class JvmPathResolver(
    private val javaFs: java.nio.file.FileSystem,
    private val fsState: NioFileSystemState,
) : PathResolver {
    override fun resolve(
        path: VirtualPath?,
        baseDirectory: BaseDirectory,
        allowEmptyPath: Boolean,
        followSymlinks: Boolean,
    ): Either<ResolvePathError, Path> {
        val pathIsAbsolute = (path ?: "").startsWith("/")
        val nioPath = try {
            val pathString = path ?: ""
            javaFs.getPath(pathString)
        } catch (@Suppress("SwallowedException") ipe: InvalidPathException) {
            return InvalidPath("Path `$path` cannot be converted").left()
        }

        if (nioPath.pathString.isEmpty() && !allowEmptyPath) {
            return EmptyPath("Empty path is not allowed").left()
        }

        val baseDirectoryPath: Either<ResolvePathError, Path> = when (baseDirectory) {
            CurrentWorkingDirectory -> javaFs.getPath("").right()
            is DirectoryFd -> when (val fdResource = fsState.get(baseDirectory.fd)) {
                null -> FileDescriptorNotOpen("Directory File descriptor ${baseDirectory.fd} is not open").left()
                !is NioDirectoryFdResource -> NotDirectory("Base path `$path` is not a directory").left()
                else -> fdResource.path.right()
            }
        }.flatMap { basePath ->
            if (basePath.isDirectory(options = asLinkOptions(followSymlinks))) {
                basePath.right()
            } else {
                NotDirectory("Base path `$path` is not a directory").left()
            }
        }

        return baseDirectoryPath.flatMap {
            if (fsState.isRootAccessAllowed) {
                it.resolve(nioPath).normalize().right()
            } else {
                it.resolveBeneath(nioPath, pathIsAbsolute)
            }
        }
    }

    private companion object {
        private fun Path.resolveBeneath(other: Path, otherSourceIsAbsolute: Boolean): Either<ResolvePathError, Path> {
            if (otherSourceIsAbsolute || other.isAbsolute) {
                return AbsolutePath("Opening file relative to directory with absolute path").left()
            }
            var path = this
            other.forEach { subpath ->
                path = path.resolve(subpath).normalize()
                if (!path.startsWith(this)) {
                    return PathOutsideOfRootPath(
                        "Path contains .. component leading to directory outside of base path",
                    ).left()
                }
            }
            require(path == path.normalize()) { "`$path` != `${path.normalize()}`" }
            return path.right()
        }
    }
}
