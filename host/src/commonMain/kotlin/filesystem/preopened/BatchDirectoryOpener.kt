/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.preopened

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath

internal abstract class BatchDirectoryOpener<P : RealPath, D : Any>(
    private val pathFactory: RealPath.Factory<P>,
) {
    private val currentDirectoryVirtualPath =
        VirtualPath.create(".").getOrElse { error("Can not create virtual path for CWD") }

    internal fun preopen(
        currentWorkingDirectoryPath: String?,
        preopenedDirectories: List<PreopenedDirectory> = listOf(),
    ): Either<DirectoryOpenError, PreopenedDirectories<D>> {
        val cwdResource: Either<OpenError, D> =
            if (currentWorkingDirectoryPath != null) {
                pathFactory.create(currentWorkingDirectoryPath)
                    .mapLeft { it.toResolveRelativePathErrors() }
                    .flatMap { cwdRealPath ->
                        preopenDirectory(
                            path = cwdRealPath,
                            virtualPath = currentDirectoryVirtualPath,
                            baseDirectoryFd = null,
                        )
                    }
            } else {
                NoEntry("Current working directory not set").left()
            }

        val opened: MutableList<D> = mutableListOf()
        val directories: Either<DirectoryOpenError, PreopenedDirectories<D>> = either {
            preopenedDirectories.toSet().mapTo(opened) { directory ->
                directory.preopen(cwdResource.getOrNull()).bind()
            }
            PreopenedDirectories(cwdResource, opened)
        }.onLeft {
            opened.closeSilent()
        }
        return directories
    }

    private fun PreopenedDirectory.preopen(
        cwd: D?,
    ): Either<DirectoryOpenError, D> = pathFactory.create(realPath)
        .mapLeft<ResolveRelativePathErrors>(PathError::toResolveRelativePathErrors)
        .flatMap { realPath -> preopenDirectory(realPath, virtualPath, cwd) }
        .mapLeft { DirectoryOpenError(this, it) }

    abstract fun preopenDirectory(
        path: P,
        virtualPath: VirtualPath,
        baseDirectoryFd: D?,
    ): Either<OpenError, D>

    abstract fun closeResource(resource: D): Either<CloseError, Unit>

    private fun Collection<D>.closeSilent(): Unit = this.forEach { dsResource ->
        closeResource(dsResource).onLeft {
            // IGNORE
        }
    }

    internal data class PreopenedDirectories<D : Any>(
        val currentWorkingDirectory: Either<OpenError, D>,
        val preopenedDirectories: List<D>,
    )

    internal data class DirectoryOpenError(
        val directory: PreopenedDirectory,
        val error: OpenError,
    )
}
