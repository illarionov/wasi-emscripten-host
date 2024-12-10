/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.apple.nativefunc.appleReadLink
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.path.real.posix.PosixPathConverter.toVirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath

internal class AppleReadLink(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<ReadLink, ReadLinkError, VirtualPath> {
    override fun invoke(input: ReadLink): Either<ReadLinkError, VirtualPath> =
        fsState.executeWithPath(input.path, input.baseDirectory) { basePath, baseDirectory ->
            appleReadLink(baseDirectory, basePath)
        }.flatMap { targetRealPath ->
            toVirtualPath(targetRealPath)
        }
}
