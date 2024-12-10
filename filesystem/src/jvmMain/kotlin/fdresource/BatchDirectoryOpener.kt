/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.fdrights.FdRightsBlock.Companion.DIRECTORY_BASE_RIGHTS_BLOCK
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.nio.NioPathConverter
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.real.nio.NioRealPath.Companion.isDirectory
import at.released.weh.filesystem.path.real.nio.NioRealPath.Companion.resolveAbsolutePath
import at.released.weh.filesystem.path.real.nio.NioRealPath.NioRealPathFactory
import at.released.weh.filesystem.path.toCommonError
import at.released.weh.filesystem.preopened.PreopenedDirectory
import java.nio.file.FileSystem as NioFileSystem

internal class BatchDirectoryOpener(
    private val fileSystem: NioFileSystem,
    private val pathFactory: NioRealPathFactory = NioRealPathFactory(fileSystem),
) {
    fun preopen(
        currentWorkingDirectoryPath: String?,
        preopenedDirectories: List<PreopenedDirectory> = listOf(),
    ): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
        val realCwd: NioRealPath = pathFactory.create(fileSystem.getPath("."))

        val currentWorkingDirectory: Either<OpenError, NioDirectoryFdResource> =
            if (currentWorkingDirectoryPath != null) {
                pathFactory.create(currentWorkingDirectoryPath)
                    .mapLeft { it.toCommonError() }
                    .flatMap { cwdRealPath ->
                        preopenDirectory(
                            preopenedDirectory = cwdRealPath,
                            basePath = realCwd,
                        )
                    }
            } else {
                NoEntry("Current working directory not set").left()
            }

        val baseCwdPath: NioRealPath = currentWorkingDirectory.fold(
            ifLeft = { realCwd },
            ifRight = NioDirectoryFdResource::path,
        )

        val opened: MutableMap<String, NioDirectoryFdResource> = mutableMapOf()
        return either {
            for (directory in preopenedDirectories) {
                val realPathString = directory.realPath
                opened.getOrPut(realPathString) {
                    pathFactory.create(realPathString)
                        .mapLeft<ResolveRelativePathErrors>(PathError::toCommonError)
                        .flatMap { realPath -> preopenDirectory(realPath, baseCwdPath) }
                        .mapLeft { BatchDirectoryOpenerError(directory, it) }
                        .bind()
                }
            }
            PreopenedDirectories(currentWorkingDirectory, opened)
        }
    }

    private fun preopenDirectory(
        preopenedDirectory: NioRealPath,
        basePath: NioRealPath,
    ): Either<OpenError, NioDirectoryFdResource> {
        val virtualPath = NioPathConverter().toVirtualPath(preopenedDirectory)
            .getOrElse { return it.toCommonError().left() }

        return NioRealPath.resolve(preopenedDirectory, basePath)
            .map { it.resolveAbsolutePath() }
            .mapLeft { it.toCommonError() }
            .flatMap { path ->
                if (!path.isDirectory()) {
                    NotDirectory("`$path` is not a directory").left()
                } else {
                    NioDirectoryFdResource(
                        path,
                        virtualPath = virtualPath,
                        isPreopened = true,
                        rights = DIRECTORY_BASE_RIGHTS_BLOCK,
                    ).right()
                }
            }
    }

    class PreopenedDirectories(
        val currentWorkingDirectory: Either<OpenError, NioDirectoryFdResource>,
        val preopenedDirectories: Map<String, NioDirectoryFdResource>,
    )

    internal data class BatchDirectoryOpenerError(
        val directory: PreopenedDirectory,
        val error: OpenError,
    )
}
