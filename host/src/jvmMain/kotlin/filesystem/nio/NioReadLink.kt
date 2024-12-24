/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.fdresource.nio.readSymbolicLink
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.path.real.nio.NioPathConverter
import at.released.weh.filesystem.path.real.nio.NioRealPath
import at.released.weh.filesystem.path.real.nio.NioRealPath.NioRealPathFactory
import at.released.weh.filesystem.path.virtual.VirtualPath

internal class NioReadLink(
    private val fsState: NioFileSystemState,
    private val pathConverter: NioPathConverter = NioPathConverter(fsState.javaFs),
    private val pathFactory: NioRealPathFactory = NioRealPathFactory(fsState.javaFs),
) : FileSystemOperationHandler<ReadLink, ReadLinkError, VirtualPath> {
    override fun invoke(input: ReadLink): Either<ReadLinkError, VirtualPath> =
        fsState.executeWithPath(input.baseDirectory, input.path) { resolvePathResult ->
            resolvePathResult.mapLeft(OpenError::toReadlinkError)
                .flatMap { path -> readSymbolicLink(path.nio) }
                .map<NioRealPath>(pathFactory::create)
                .flatMap { target ->
                    pathConverter.toVirtualPath(target)
                        .mapLeft { error -> InvalidArgument(error.message) }
                }
        }
}

private fun OpenError.toReadlinkError(): ReadLinkError = when (this) {
    is ReadLinkError -> this
    else -> IoError(this.message)
}
