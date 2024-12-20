/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock.Companion.DIRECTORY_BASE_RIGHTS_BLOCK
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.real.nio.NioRealPath.Companion.isDirectory
import at.released.weh.filesystem.path.real.nio.NioRealPath.Companion.resolveAbsolutePath
import at.released.weh.filesystem.path.real.nio.NioRealPath.NioRealPathFactory
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.preopened.BatchDirectoryOpener
import java.nio.file.FileSystem as NioFileSystem

internal class NioDirectoryOpener(
    private val fileSystem: NioFileSystem,
    private val pathFactory: NioRealPathFactory = NioRealPathFactory(fileSystem),
) : BatchDirectoryOpener<NioRealPath, NioDirectoryFdResource>(pathFactory) {
    override fun preopenDirectory(
        path: NioRealPath,
        virtualPath: VirtualPath,
        baseDirectoryFd: NioDirectoryFdResource?,
    ): Either<OpenError, NioDirectoryFdResource> {
        val baseCwdPath: NioRealPath =
            baseDirectoryFd?.let(NioDirectoryFdResource::path) ?: pathFactory.create(fileSystem.getPath("."))

        return NioRealPath.resolve(path, baseCwdPath)
            .map { it.resolveAbsolutePath() }
            .mapLeft { it.toResolveRelativePathErrors() }
            .flatMap { absolutePath: NioRealPath ->
                if (!absolutePath.isDirectory()) {
                    NotDirectory("`$absolutePath` is not a directory").left()
                } else {
                    NioDirectoryFdResource(
                        absolutePath,
                        virtualPath = virtualPath,
                        isPreopened = true,
                        rights = DIRECTORY_BASE_RIGHTS_BLOCK,
                    ).right()
                }
            }
    }

    override fun closeResource(resource: NioDirectoryFdResource): Either<CloseError, Unit> {
        return resource.close()
    }
}
