/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")

package at.released.weh.filesystem.posix.fdresource

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.ext.flatMapLeft
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.model.Filetype.SYMBOLIC_LINK
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.SymlinkResolver
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Directory
import at.released.weh.filesystem.path.real.posix.PosixPathConverter.toVirtualPath
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.withPathErrorAsCommonError
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor.ExecutionBlock
import at.released.weh.filesystem.posix.nativefunc.posixClose
import platform.posix.dup
import at.released.weh.filesystem.error.IoError as FileSystemIoError

/**
 * Implementation of FileSystemActionExecutor that uses manual path resolution with symlink expansion for environments
 * where RESOLVE_BENEATH is not available.
 *
 * path in [block] is a basename of path
 */
internal class NonSystemFileSystemActionExecutor(
    private val pathResolver: PosixPathResolver,
    private val openDirectoryFunction: (
        base: NativeDirectoryFd,
        path: PosixRealPath,
        isBasename: Boolean,
    ) -> Either<OpenError, NativeDirectoryFd>,
    private val statFunction: (base: NativeDirectoryFd, path: PosixRealPath) -> Either<StatError, StructStat>,
    private val readLinkFunction: (
        base: NativeDirectoryFd,
        path: PosixRealPath,
        initialBufSize: Int,
    ) -> Either<ReadLinkError, PosixRealPath>,
) : FileSystemActionExecutor {
    override fun <E : FileSystemOperationError, R : Any> executeWithPath(
        path: VirtualPath,
        baseDirectory: BaseDirectory,
        followBaseSymlink: Boolean,
        errorMapper: (ResolvePathError) -> E,
        block: ExecutionBlock<E, R>,
    ): Either<E, R> {
        return pathResolver.getBaseDirectory(baseDirectory)
            .mapLeft { errorMapper(it) }
            .flatMap { directory: PosixDirectoryChannel ->
                executeWithPath(
                    path,
                    directory,
                    followBaseSymlink,
                    errorMapper,
                    block,
                )
            }
    }

    private fun <E : FileSystemOperationError, R : Any> executeWithPath(
        path: VirtualPath,
        baseDirectoryChannel: PosixDirectoryChannel,
        followBaseSymlink: Boolean,
        errorMapper: (ResolvePathError) -> E,
        block: ExecutionBlock<E, R>,
    ): Either<E, R> {
        val initialHandle = OpenComponent(baseDirectoryChannel.nativeFd, null)

        val symlinkResolver: SymlinkResolver<OpenComponent> = SymlinkResolver(
            base = Directory(initialHandle),
            path = path,
            followBasenameSymlink = followBaseSymlink,
            openFunction = { base, component, isBasename ->
                openComponent(
                    base,
                    component,
                    isBasename,
                    followBaseSymlink,
                )
            },
            closeFunction = ::close,
        )

        return symlinkResolver.resolve()
            .mapLeft { errorMapper(it) }
            .flatMap { resolvedPath: Subcomponent<OpenComponent> ->
                try {
                    val newHandle = resolvedPath.handle
                    val newBasename = newHandle.fileBasename ?: POSIX_PATH_CURRENT_DIR

                    val newChannel: PosixDirectoryChannel = baseDirectoryChannel.copy(
                        nativeFd = newHandle.descriptor,
                    )
                    block(newBasename, newChannel, false)
                } finally {
                    resolvedPath.handle.descriptor.let { pathDescriptor ->
                        if (pathDescriptor != baseDirectoryChannel.nativeFd) {
                            posixClose(pathDescriptor).onLeft { /* ignore */ }
                        }
                    }
                }
            }
    }

    private fun openComponent(
        base: Directory<OpenComponent>,
        component: String,
        isBasename: Boolean,
        followBaseSymlink: Boolean,
    ): Either<OpenError, Subcomponent<OpenComponent>> {
        val baseFd: NativeDirectoryFd = base.handle.descriptor
        val componentAsPath = PosixRealPath.create(component).withPathErrorAsCommonError().getOrElse {
            return it.left()
        }

        return if (!isBasename) {
            openNonBasenameComponent(baseFd, componentAsPath)
        } else {
            openBasenameComponent(baseFd, componentAsPath, followBaseSymlink)
        }
    }

    private fun openNonBasenameComponent(
        base: NativeDirectoryFd,
        componentAsPath: PosixRealPath,
    ): Either<OpenError, Subcomponent<OpenComponent>> {
        return openDirectoryFunction(base, componentAsPath, false)
            .map { newDirectoryFd -> Directory(OpenComponent(newDirectoryFd, null)) }
            .flatMapLeft { directoryOpenError: OpenError ->
                when {
                    directoryOpenError is NotDirectory -> readSubcomponentNonDirectory(base, componentAsPath)
                    else -> directoryOpenError.left()
                }
            }
    }

    private fun readSubcomponentNonDirectory(
        base: NativeDirectoryFd,
        path: PosixRealPath,
    ): Either<OpenError, Subcomponent<OpenComponent>> = statFunction(base, path)
        .mapLeft { it.toOpenError() }
        .flatMap { stat ->
            when (stat.type) {
                DIRECTORY -> IoError("File type changed during resolving").left()
                SYMBOLIC_LINK -> openSymbolicLinkSubcomponent(base, path, stat.size.toInt())
                else -> dupfd(base).map { Subcomponent.Other(OpenComponent(it, path)) }
            }
        }

    private fun openBasenameComponent(
        base: NativeDirectoryFd,
        componentAsPath: PosixRealPath,
        followBaseSymlink: Boolean,
    ): Either<OpenError, Subcomponent<OpenComponent>> = if (!followBaseSymlink) {
        openOtherSubcomponent(base, componentAsPath)
    } else {
        statFunction(base, componentAsPath)
            .fold(
                ifLeft = { statError ->
                    if (statError is NoEntry) {
                        openOtherSubcomponent(base, componentAsPath)
                    } else {
                        statError.toOpenError().left()
                    }
                },
                ifRight = { stat ->
                    if (stat.type == SYMBOLIC_LINK) {
                        openSymbolicLinkSubcomponent(base, componentAsPath, stat.size.toInt())
                    } else {
                        openOtherSubcomponent(base, componentAsPath)
                    }
                },
            )
    }

    private fun openSymbolicLinkSubcomponent(
        base: NativeDirectoryFd,
        componentAsPath: PosixRealPath,
        initialBufferSize: Int,
    ): Either<OpenError, Subcomponent.Symlink<OpenComponent>> = either {
        val target: VirtualPath = readLinkFunction(base, componentAsPath, initialBufferSize)
            .mapLeft<OpenError>(ReadLinkError::toOpenError)
            .flatMap { targetPath: PosixRealPath -> toVirtualPath(targetPath).withPathErrorAsCommonError() }
            .bind()
        val newfd = dupfd(base).bind()
        Subcomponent.Symlink(OpenComponent(newfd, componentAsPath), target)
    }

    private fun openOtherSubcomponent(
        base: NativeDirectoryFd,
        componentAsPath: PosixRealPath,
    ): Either<OpenError, Subcomponent.Other<OpenComponent>> = dupfd(base).map { newfd: NativeDirectoryFd ->
        Subcomponent.Other(OpenComponent(newfd, componentAsPath))
    }

    private fun close(
        component: Subcomponent<OpenComponent>,
    ): Either<CloseError, Unit> = posixClose(component.handle.descriptor)

    private fun dupfd(fd: NativeDirectoryFd): Either<Mfile, NativeDirectoryFd> {
        val newfd = dup(fd.raw)
        return if (newfd != -1) {
            NativeDirectoryFd(newfd).right()
        } else {
            Mfile("Can not dup fd").left()
        }
    }
}

private data class OpenComponent(
    val descriptor: NativeDirectoryFd,
    val fileBasename: PosixRealPath?,
)

private val POSIX_PATH_CURRENT_DIR = PosixRealPath.create(".").getOrElse { error("Can not create path") }

private fun StatError.toOpenError(): OpenError = if (this is OpenError) {
    this
} else {
    FileSystemIoError(this.message)
}

private fun ReadLinkError.toOpenError(): OpenError = if (this is OpenError) {
    this
} else {
    FileSystemIoError(this.message)
}
