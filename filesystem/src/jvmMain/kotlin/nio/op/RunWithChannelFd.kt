/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio.op

import arrow.core.Either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.fdresource.nio.NioFileChannel
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

public class RunWithChannelFd<R>(
    @IntFileDescriptor
    public val fd: FileDescriptor,
    public val block: (
        channel: Either<BadFileDescriptor, NioFileChannel>,
    ) -> Either<FileSystemOperationError, R>,
    public val nonNioResourceFallback: () -> Either<FileSystemOperationError, R>,
) {
    public companion object : FileSystemOperation<RunWithChannelFd<Any>, FileSystemOperationError, Any> {
        override val tag: String = "runwithchanfd"
        public fun <R : Any> key(): FileSystemOperation<RunWithChannelFd<R>, FileSystemOperationError, R> {
            @Suppress("UNCHECKED_CAST")
            return Companion as FileSystemOperation<RunWithChannelFd<R>, FileSystemOperationError, R>
        }
    }
}
