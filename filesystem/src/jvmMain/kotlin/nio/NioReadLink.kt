/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.fdresource.nio.readSymbolicLink
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.nio.path.JvmNioPathConverter
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.path.virtual.VirtualPath
import java.nio.file.Path

internal class NioReadLink(
    private val fsState: NioFileSystemState,
    private val pathConverter: JvmNioPathConverter = JvmNioPathConverter(fsState.javaFs),
) : FileSystemOperationHandler<ReadLink, ReadLinkError, VirtualPath> {
    override fun invoke(input: ReadLink): Either<ReadLinkError, VirtualPath> =
        fsState.executeWithPath(input.baseDirectory, input.path) { resolvePathResult ->
            resolvePathResult.mapLeft(ResolvePathError::toCommonError)
                .flatMap { path: Path -> readSymbolicLink(path) }
                .flatMap { target ->
                    pathConverter.toVirtualPath(target)
                        .mapLeft { error -> InvalidArgument(error.message) }
                }
        }
}
