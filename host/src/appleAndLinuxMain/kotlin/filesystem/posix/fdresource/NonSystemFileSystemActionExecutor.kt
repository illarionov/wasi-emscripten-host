/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.fdresource

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import arrow.core.raise.either
import at.released.weh.ext.flatMapLeft
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.path.PathError.OtherOpenError
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.SymlinkResolver
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Directory
import at.released.weh.filesystem.path.real.posix.PosixPathConverter.toVirtualPath
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.withPathErrorAsCommonError
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.nativefunc.posixClose
import at.released.weh.filesystem.error.IoError as FileSystemIoError
import platform.posix.dup

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
    private val statFunction: (
        base: NativeDirectoryFd,
        path: PosixRealPath,
    ) -> Either<StatError, StructStat>,
    private val readLinkFunction: (
        base: NativeDirectoryFd,
        path: PosixRealPath,
        initialBuf: Int,
    ) -> Either<ReadLinkError, PosixRealPath>,
) : FileSystemActionExecutor {
    override fun <E : FileSystemOperationError, R : Any> executeWithPath(
        path: VirtualPath,
        baseDirectory: BaseDirectory,
        errorMapper: (ResolvePathError) -> E,
        block: (path: PosixRealPath, baseDirectory: PosixDirectoryChannel) -> Either<E, R>,
    ): Either<E, R> {
        return pathResolver.getBaseDirectory(baseDirectory)
            .mapLeft { errorMapper(it) }
            .flatMap { nativeChannel: PosixDirectoryChannel ->
                executeWithPath(path, nativeChannel, errorMapper, block)
            }
    }

    private fun <E : FileSystemOperationError, R : Any> executeWithPath(
        path: VirtualPath,
        baseDirectoryChannel: PosixDirectoryChannel,
        errorMapper: (ResolvePathError) -> E,
        block: (path: PosixRealPath, baseDirectory: PosixDirectoryChannel) -> Either<E, R>,
    ): Either<E, R> {
        val initialHandle = OpenComponent(baseDirectoryChannel.nativeFd, null)

        val directoryResolver: SymlinkResolver<OpenComponent> = SymlinkResolver(
            base = Directory(initialHandle),
            path = path,
            followBasenameSymlink = true,
            openFunction = ::openComponent,
            closeFunction = ::close,
        )

        return directoryResolver.resolve()
            .mapLeft { errorMapper(OtherOpenError(it)) } // TODO: remove
            .flatMap { resolvedPath: Subcomponent<OpenComponent> ->
                try {
                    val newHandle = resolvedPath.handle
                    if (newHandle.basename == null) {
                        error("Unexpected new basename")
                    }
                    val newChannel = baseDirectoryChannel.copy(nativeFd = newHandle.descriptor)
                    block(newHandle.basename, newChannel)
                } finally {
                    resolvedPath.handle.descriptor.let {
                        if (it != baseDirectoryChannel.nativeFd && it != null) {
                            posixClose(it).onLeft {
                                /* ignore */
                                // TODO: do not ignore
                            }
                        }
                    }
                }
            }
    }

    private fun openComponent(
        base: Directory<OpenComponent>,
        component: String,
        isBasename: Boolean,
    ): Either<OpenError, Subcomponent<OpenComponent>> = either {
        val componentAsPath = PosixRealPath.create(component).withPathErrorAsCommonError().bind()
        val baseFd: NativeDirectoryFd = base.handle.descriptor

        val stat: StructStat? = statFunction(baseFd, componentAsPath)
            .flatMapLeft<StatError, StructStat?, StatError> { statError: StatError ->
                if (isBasename && statError is NoEntry) {
                    null.right()
                } else {
                    statError.left()
                }
            }
            .mapLeft { it.toOpenError() }
            .bind()

        when (stat?.type) {
            Filetype.DIRECTORY -> {
                val newDirectoryFd: NativeDirectoryFd =
                    openDirectoryFunction(baseFd, componentAsPath, isBasename)
                        .bind()
                Subcomponent.Directory(OpenComponent(newDirectoryFd, componentAsPath))
            }

            Filetype.SYMBOLIC_LINK -> {
                val target: VirtualPath = readLinkFunction(baseFd, componentAsPath, stat.size.toInt())
                    .mapLeft<OpenError>(ReadLinkError::toOpenError)
                    .flatMap { targetPath: PosixRealPath -> toVirtualPath(targetPath).withPathErrorAsCommonError() }
                    .bind()
                val newfd = dupfd(baseFd).bind()
                Subcomponent.Symlink(OpenComponent(newfd, componentAsPath), target)
            }

            else -> {
                val newfd = dupfd(baseFd).bind()

                Subcomponent.Other(OpenComponent(base.handle.descriptor, componentAsPath))
            }
        }
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

    private class OpenComponent(
        val descriptor: NativeDirectoryFd,
        val basename: PosixRealPath?,
    )
}

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
