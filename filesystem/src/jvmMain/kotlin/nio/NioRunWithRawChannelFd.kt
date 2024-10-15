/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.fdresource.NioFileFdResource
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.op.RunWithChannelFd
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenMessage
import kotlin.concurrent.withLock

internal class NioRunWithRawChannelFd<R : Any> internal constructor(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<RunWithChannelFd<R>, FileSystemOperationError, R> {
    override fun invoke(input: RunWithChannelFd<R>): Either<FileSystemOperationError, R> =
        @Suppress("IfThenToElvis")
        fsState.executeWithResource(input.fd) { fdResource ->
            val nioFileResource = fdResource as? NioFileFdResource
            if (nioFileResource != null) {
                nioFileResource.lock.withLock {
                    input.block(nioFileResource.channel.channel.right())
                }
            } else {
                input.block(
                    BadFileDescriptor(fileDescriptorNotOpenMessage(input.fd)).left(),
                )
            }
        }
}
