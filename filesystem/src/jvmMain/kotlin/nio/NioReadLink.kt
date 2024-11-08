/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.fdresource.nio.readSymbolicLink
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.readlink.ReadLink

internal class NioReadLink(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<ReadLink, ReadLinkError, String> {
    override fun invoke(input: ReadLink): Either<ReadLinkError, String> =
        fsState.executeWithPath(input.baseDirectory, input.path) {
            it.mapLeft(ResolvePathError::toCommonError).flatMap(::readSymbolicLink)
        }
}
