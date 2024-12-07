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
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock.Companion.DIRECTORY_BASE_RIGHTS_BLOCK
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.preopened.PreopenedDirectory
import java.io.IOError
import java.nio.file.InvalidPathException
import java.nio.file.Path
import kotlin.io.path.isDirectory
import java.nio.file.FileSystem as NioFileSystem

internal class BatchDirectoryOpener(
    private val fileSystem: NioFileSystem,
) {
    fun preopen(
        currentWorkingDirectoryPath: RealPath = "",
        preopenedDirectories: List<PreopenedDirectory> = listOf(),
    ): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
        val realCwd: Path = fileSystem.getPath("")
        val currentWorkingDirectory: Either<OpenError, NioDirectoryFdResource> = preopenDirectory(
            preopenedDirectory = currentWorkingDirectoryPath,
            basePath = realCwd,
        )

        val baseCwdPath: Path = currentWorkingDirectory.fold(
            ifLeft = { realCwd },
            ifRight = NioDirectoryFdResource::path,
        )

        val opened: MutableMap<RealPath, NioDirectoryFdResource> = mutableMapOf()
        return either {
            for (directory in preopenedDirectories) {
                val realpath = directory.realPath

                if (opened.containsKey(realpath)) {
                    continue
                }

                val fdResource = preopenDirectory(realpath, baseCwdPath)
                    .mapLeft { BatchDirectoryOpenerError(directory, it) }
                    .bind()

                opened[realpath] = fdResource
            }

            PreopenedDirectories(currentWorkingDirectory, opened)
        }
    }

    private fun preopenDirectory(
        preopenedDirectory: RealPath,
        basePath: Path,
    ): Either<OpenError, NioDirectoryFdResource> {
        val virtualPath = convertRealPathToVirtualPath(preopenedDirectory)
            .getOrElse { return it.left() }

        return Either.catch {
            basePath.resolve(preopenedDirectory).toAbsolutePath()
        }.mapLeft { ex ->
            when (ex) {
                is InvalidPathException -> InvalidArgument("Can not resolve path `$preopenedDirectory`")
                is IOError -> IoError(ex.message.toString())
                else -> throw ex
            }
        }.flatMap { path ->
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

    private fun convertRealPathToVirtualPath(
        path: RealPath,
    ): Either<InvalidArgument, VirtualPath> {
        return VirtualPath.of(path)
            .mapLeft { InvalidArgument(it.message) }
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
