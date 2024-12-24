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
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Directory
import at.released.weh.filesystem.path.SymlinkResolver.Subcomponent.Other
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute

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

    fun resolve(): Either<OpenError, Subcomponent<H>> {
        openedComponents.addLast(base)
        path.splitToComponents().flatMap(::pushSymlink).onLeft { return it.left() }

        return try {
            resolveInternal()
        } finally {
            closeHandles()
        }
    }

    private fun resolveInternal(): Either<OpenError, Subcomponent<H>> = either {
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
                            raise(NotCapable("Path outside of root path"))
                        } else {
                            closeFunction(dropped).mapLeft { it.toOpenError() }.bind()
                        }
                    }

                    else -> openComponent(component).bind()
                }
            }
        }

        val lastHandle: Subcomponent<H> =
            openedComponents.removeLastOrNull() ?: raise(NotCapable("Path outside of root"))
        if (lastHandle == base) {
            // Duplicate handle
            return openFunction(base, ".", true)
        }
        return lastHandle.right()
    }

    private fun openComponent(
        component: String,
    ): Either<OpenError, Unit> = either {
        val isBasename = paths.all { it.isEmpty() }

        val rootHandle = openedComponents.last()
        if (rootHandle !is Directory<H>) {
            raise(NotDirectory("A component in path is not a directory"))
        }
        val componentHandle = openFunction(rootHandle, component, isBasename).bind()
        when (componentHandle) {
            is Directory<H> -> addHandle(componentHandle).bind()
            is Subcomponent.Symlink<H> -> if (isBasename && !followBasenameSymlink) {
                addHandle(componentHandle).bind()
            } else {
                closeFunction(componentHandle)
                componentHandle.target.splitToComponents()
                    .flatMap(::pushSymlink)
                    .bind()
            }

            is Other<H> -> addHandle(componentHandle).bind()
        }
    }

    private fun VirtualPath.splitToComponents(): Either<OpenError, ArrayDeque<String>> {
        if (this.isAbsolute()) {
            return NotCapable("Absolute path").left()
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
            InvalidArgument("Empty path").left()
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

    private fun CloseError.toOpenError(): OpenError = if (this is OpenError) {
        this
    } else {
        IoError(this.message)
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
