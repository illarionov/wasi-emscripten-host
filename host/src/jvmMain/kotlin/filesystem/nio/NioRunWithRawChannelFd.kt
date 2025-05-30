/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.fdresource.NioFileFdResource
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.nio.op.RunWithChannelFd
import kotlin.concurrent.withLock

internal class NioRunWithRawChannelFd<R : Any> internal constructor(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<RunWithChannelFd<R>, FileSystemOperationError, R> {
    override fun invoke(input: RunWithChannelFd<R>): Either<FileSystemOperationError, R> =
        fsState.executeWithResource(input.fd) { fdResource: FdResource ->
            val nioFileResource = fdResource as? NioFileFdResource
            nioFileResource?.lock?.withLock {
                input.block(nioFileResource.channel.right())
            } ?: input.nonNioResourceFallback()
        }
}
