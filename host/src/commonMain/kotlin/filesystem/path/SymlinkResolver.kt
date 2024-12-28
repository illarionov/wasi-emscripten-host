/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.Mlink
import at.released.weh.filesystem.error.Nfile
import at.released.weh.filesystem.error.NoSpace
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.path.PathError.AbsolutePath
import at.released.weh.filesystem.path.PathError.NameTooLong
import at.released.weh.filesystem.path.PathError.NotDirectory
import at.released.weh.filesystem.path.PathError.OtherOpenError
import at.released.weh.filesystem.path.PathError.PathOutsideOfRootPath
import at.released.weh.filesystem.path.PathError.TooManySymbolicLinks
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Directory
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Other
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute
import at.released.weh.filesystem.error.NameTooLong as FileSystemNameTooLong
import at.released.weh.filesystem.error.NotDirectory as FileSystemNotDirectory
import at.released.weh.filesystem.error.TooManySymbolicLinks as FileSystemTooManySymbolicLinks

private const val MAX_OPEN_DIRECTORY_HANDLES = 128
private const val MAX_SYMLINKS_EXPANSIONS = 128

/**
 * Implements manual path resolution with symlink expansion according to
 * the [WASI filesystem path resolution](https://github.com/WebAssembly/wasi-filesystem/blob/main/path-resolution.md#implementing-path-resolution-manually)
 * sandboxing scheme.
 *
 * It is intended for use on systems without intra-directory path restriction mechanisms such as RESOLVE_BENEATH,
 * and symbolic links can be used for path traversal attacks to escape the sandbox.
 */
internal class SymlinkResolver<H>(
    private val base: Directory<H>,
    private val path: VirtualPath,
    private val followBasenameSymlink: Boolean,
    private val openFunction: (
        base: Directory<H>,
        component: String,
        isBasename: Boolean,
    ) -> Either<OpenError, Subcomponent<H>>,
    private val closeFunction: (Subcomponent<H>) -> Either<CloseError, Unit>,
) {
    val openedComponents: ArrayDeque<Subcomponent<H>> = ArrayDeque(MAX_OPEN_DIRECTORY_HANDLES)
    val paths: ArrayDeque<ArrayDeque<String>> = ArrayDeque()
    var symlinkExpansions: Int = 0

    fun resolve(): Either<ResolvePathError, Subcomponent<H>> {
        openedComponents.addLast(base)
        path.splitToComponents().flatMap(::pushSymlink).onLeft { return it.left() }

        return try {
            resolveInternal()
        } finally {
            closeHandles()
        }
    }

    private fun resolveInternal(): Either<ResolvePathError, Subcomponent<H>> = either {
        while (paths.isNotEmpty()) {
            val currentPath: ArrayDeque<String> = paths.last()
            if (currentPath.isEmpty()) {
                // Symlink resolved
                paths.removeLast()
            } else {
                val component: String = currentPath.removeFirst()
                when (component) {
                    "" -> Unit // skip
                    ".", "./" -> Unit // skip
                    "..", "../" -> {
                        val dropped = openedComponents.removeLast()
                        if (openedComponents.isEmpty()) {
                            raise(PathOutsideOfRootPath("Path outside of root path"))
                        } else {
                            closeFunction(dropped).mapLeft { it.toResolvePathError() }.bind()
                        }
                    }

                    else -> openComponent(component).bind()
                }
            }
        }

        val lastHandle = openedComponents.removeLastOrNull() ?: raise(PathOutsideOfRootPath("Path outside of root"))
        if (lastHandle == base) {
            // Duplicate handle
            return openFunction(base, ".", true).mapLeft(OpenError::toResolvePathError)
        }
        return lastHandle.right()
    }

    private fun openComponent(
        component: String,
    ): Either<ResolvePathError, Unit> = either {
        val isBasename = paths.all { it.isEmpty() }

        val rootHandle = openedComponents.last()
        if (rootHandle !is Directory<H>) {
            raise(NotDirectory("A component in path is not a directory"))
        }
        val componentHandle = openFunction(rootHandle, component, isBasename)
            .mapLeft(OpenError::toResolvePathError)
            .bind()

        when (componentHandle) {
            is Directory<H> -> addHandle(componentHandle).bind()
            is Subcomponent.Symlink<H> -> if (isBasename && !followBasenameSymlink) {
                addHandle(componentHandle).bind()
            } else {
                closeFunction(componentHandle).mapLeft(CloseError::toResolvePathError).bind()
                componentHandle.target.splitToComponents()
                    .flatMap(::pushSymlink)
                    .bind()
            }

            is Other<H> -> addHandle(componentHandle).bind()
        }
    }

    private fun VirtualPath.splitToComponents(): Either<ResolvePathError, ArrayDeque<String>> {
        if (this.isAbsolute()) {
            return AbsolutePath("Absolute path").left()
        }

        val components = ArrayDeque(this.toString().split("/"))
        if (components.last().isNotEmpty()) {
            return components.right()
        }
        while (components.isNotEmpty()) {
            val last: String = components.removeLast()
            if (last.isNotEmpty()) {
                components.addLast("$last/")
                break
            }
        }

        return if (components.isNotEmpty()) {
            components.right()
        } else {
            PathError.EmptyPath("Empty path").left()
        }
    }

    private fun pushSymlink(
        path: ArrayDeque<String>,
    ): Either<TooManySymbolicLinks, Unit> {
        if (symlinkExpansions == MAX_SYMLINKS_EXPANSIONS) {
            return TooManySymbolicLinks("Too many levels of symbolic links").left()
        }
        symlinkExpansions += 1
        paths.addLast(path)
        return Unit.right()
    }

    private fun addHandle(
        handle: Subcomponent<H>,
    ): Either<NameTooLong, Unit> {
        if (openedComponents.size == MAX_OPEN_DIRECTORY_HANDLES) {
            return NameTooLong("Too many handles opened").left()
        }
        openedComponents.addLast(handle)
        return Unit.right()
    }

    private fun closeHandles() {
        openedComponents.drop(1).forEach { handle ->
            closeFunction(handle).onLeft { /* ignore */ }
        }
    }

    internal sealed interface Subcomponent<H> {
        val handle: H

        data class Directory<H>(
            override val handle: H,
        ) : Subcomponent<H>

        data class Symlink<H>(
            override val handle: H,
            val target: VirtualPath,
        ) : Subcomponent<H>

        data class Other<H>(
            override val handle: H,
        ) : Subcomponent<H>
    }
}

private fun OpenError.toResolvePathError(): ResolvePathError = when (this) {
    is AccessDenied -> PathError.AccessDenied("Access to a path component was denied")
    is BadFileDescriptor -> PathError.FileDescriptorNotOpen(
        "An invalid file descriptor was used while resolving the path",
    )

    is IoError -> PathError.IoError("An I/O error occurred during path resolution")
    is Mfile -> PathError.OpenFileDescriptorLimitReached(
        "The open file descriptor limit was reached during path resolution",
    )

    is Mlink -> TooManySymbolicLinks("Too many symbolic links")
    is FileSystemNameTooLong -> NameTooLong("Name too long")
    is Nfile -> PathError.OpenFileDescriptorLimitReached(
        "The open file descriptor limit was reached during path resolution",
    )

    is NotCapable -> PathOutsideOfRootPath("Escape from root during path resolution detected")
    is FileSystemNotDirectory -> NotDirectory("Not a directory in path prefix")
    is FileSystemTooManySymbolicLinks -> TooManySymbolicLinks("Too many symbolic links")
    else -> OtherOpenError(this)
}

private fun CloseError.toResolvePathError(): ResolvePathError = when (this) {
    is BadFileDescriptor -> PathError.FileDescriptorNotOpen("Trying to close already closed file descriptor")
    is DiskQuota -> OtherOpenError(this)
    is Interrupted -> OtherOpenError(this)
    is IoError -> PathError.IoError("I/O error occurs during closing file descriptor")
    is NoSpace -> OtherOpenError(this)
}
