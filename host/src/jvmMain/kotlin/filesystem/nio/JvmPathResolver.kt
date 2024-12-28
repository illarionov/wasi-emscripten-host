/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.fdresource.NioDirectoryFdResource
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.PathError.FileDescriptorNotOpen
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.SymlinkResolver
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Directory
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Symlink
import at.released.weh.filesystem.path.real.nio.NioPathConverter
import at.released.weh.filesystem.path.real.nio.NioPathConverter.Companion.normalizeSlashes
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.real.nio.NioRealPath.NioRealPathFactory
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.withResolvePathError
import java.io.IOException
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.NotLinkException
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.readSymbolicLink

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

        return if (fsState.isRootAccessAllowed) {
            Either.zipOrAccumulate(
                { _, nioPathError -> nioPathError },
                baseDirectoryPath,
                pathConverter.toRealPath(path).withResolvePathError(),
            ) { base, subPath -> base to subPath }
                .flatMap { (base, subPath) ->
                    val finalPath = base.nio.resolve(subPath.nio).normalize()
                    pathFactory.create(finalPath).right()
                }
        } else {
            baseDirectoryPath.flatMap { base: NioRealPath ->
                NioSymlinkResolver(base, path, followSymlinks, pathConverter, pathFactory).resolve()
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

    private class NioSymlinkResolver(
        base: NioRealPath,
        path: VirtualPath,
        followBasenameSymlink: Boolean = false,
        private val pathConverter: NioPathConverter,
        private val pathFactory: NioRealPathFactory,
    ) {
        private val resolver: SymlinkResolver<NioRealPath> = SymlinkResolver(
            base = Directory(base),
            path = path,
            followBasenameSymlink = followBasenameSymlink,
            openFunction = ::nioOpen,
            closeFunction = { Unit.right() },
        )

        fun resolve(): Either<ResolvePathError, NioRealPath> {
            return resolver.resolve().map { it.handle }
        }

        private fun nioOpen(
            base: Directory<NioRealPath>,
            component: String,
            isBasename: Boolean,
        ): Either<OpenError, Subcomponent<NioRealPath>> = Either.catch {
            val newPathNio = if (component == ".") {
                base.handle.nio
            } else {
                base.handle.nio.resolve(normalizeSlashes(component))
            }
            when {
                newPathNio.isSymbolicLink() -> {
                    val target: Path = newPathNio.readSymbolicLink()
                    val targetRealPath = pathFactory.create(target)
                    val targetVirtualPath = pathConverter.toVirtualPath(targetRealPath).getOrElse {
                        throw OpenErrorException(InvalidArgument("Can not convert symlink to virtual path"))
                    }
                    Symlink(pathFactory.create(newPathNio), targetVirtualPath)
                }

                newPathNio.isDirectory(NOFOLLOW_LINKS) -> Directory(pathFactory.create(newPathNio))
                !isBasename && !newPathNio.exists(NOFOLLOW_LINKS) -> throw OpenErrorException(NoEntry("File not found"))
                else -> Subcomponent.Other(pathFactory.create(newPathNio))
            }
        }.mapLeft { throwable ->
            val error: OpenError = when (throwable) {
                is OpenErrorException -> throwable.openError
                is UnsupportedOperationException -> InvalidArgument("Operation not supported")
                is NotLinkException -> InvalidArgument("Not a symlink")
                is IOException -> IoError("I/o error while read path")
                is SecurityException -> AccessDenied("Permission denied")
                else -> throw IllegalStateException("Unexpected error", throwable)
            }
            error
        }
    }

    private class OpenErrorException(val openError: OpenError) : IOException(openError.message)
}
