/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.fdresource.NioDirectoryFdResource
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.PathError.FileDescriptorNotOpen
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.nio.NioPathConverter
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.real.nio.NioRealPath.NioRealPathFactory
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute
import at.released.weh.filesystem.path.withResolvePathError
import kotlin.io.path.isDirectory

internal class JvmPathResolver(
    private val javaFs: java.nio.file.FileSystem,
    private var currentWorkingDirectoryFd: FileDescriptor,
    private val fsState: NioFileSystemState,
    private val pathConverter: NioPathConverter = NioPathConverter(javaFs),
    private val pathFactory: NioRealPathFactory = NioRealPathFactory(javaFs),
) {
    fun resolve(
        path: VirtualPath,
        baseDirectory: BaseDirectory,
        followSymlinks: Boolean,
    ): Either<ResolvePathError, NioRealPath> {
        val baseDirectoryPath: Either<ResolvePathError, NioRealPath> = when (baseDirectory) {
            CurrentWorkingDirectory -> getDirectoryChannel(currentWorkingDirectoryFd)
            is DirectoryFd -> getDirectoryChannel(baseDirectory.fd)
        }.flatMap { directoryChannel: NioDirectoryFdResource ->
            val basePath = directoryChannel.path
            // check existence
            if (basePath.nio.isDirectory(options = asLinkOptions(followSymlinks))) {
                basePath.right()
            } else {
                PathError.NotDirectory("Base path `$path` is not a directory").left()
            }
        }

        val nioPath: Either<ResolvePathError, NioRealPath> = pathConverter.toRealPath(path).withResolvePathError()

        return Either.zipOrAccumulate(
            { _, nioPathError -> nioPathError },
            baseDirectoryPath,
            nioPath,
        ) { base, subPath -> base to subPath }
            .flatMap { (base, subPath) ->
                if (fsState.isRootAccessAllowed) {
                    val finalPath = base.nio.resolve(subPath.nio).normalize()
                    pathFactory.create(finalPath).right()
                } else {
                    base.resolveBeneath(subPath, path.isAbsolute())
                }
            }
    }

    private fun getDirectoryChannel(fd: FileDescriptor): Either<ResolvePathError, NioDirectoryFdResource> {
        return when (val fdResource = fsState.get(fd)) {
            null -> FileDescriptorNotOpen("Directory File descriptor $fd is not open").left()
            !is NioDirectoryFdResource -> PathError.NotDirectory("Not a directory").left()
            else -> fdResource.right()
        }
    }

    private fun NioRealPath.resolveBeneath(
        other: NioRealPath,
        otherSourceIsAbsolute: Boolean,
    ): Either<ResolvePathError, NioRealPath> {
        if (otherSourceIsAbsolute || other.isAbsolute) {
            return PathError.AbsolutePath("Opening file relative to directory with absolute path").left()
        }
        val thisNioPath = this.nio

        var path = this.nio
        other.nio.forEach { subpath ->
            path = path.resolve(subpath).normalize()
            if (!path.startsWith(thisNioPath)) {
                return PathError.PathOutsideOfRootPath(
                    "Path contains .. component leading to directory outside of base path",
                ).left()
            }
        }
        require(path == path.normalize()) { "`$path` != `${path.normalize()}`" }
        return pathFactory.create(path).right()
    }
}
