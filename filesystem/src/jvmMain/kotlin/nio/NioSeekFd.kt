/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.fdresource.nio.ChannelPositionError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.op.seek.SeekFd

internal class NioSeekFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<SeekFd, SeekError, Long> {
    override fun invoke(input: SeekFd): Either<SeekError, Long> {
        val nioFileResource: FdResource = fsState.get(input.fd)
            ?: return BadFileDescriptor("File descriptor `${input.fd}` is not opened or not seekable").left()
        return nioFileResource.seek(input.fileDelta, input.whence)
    }

    companion object {
        fun ChannelPositionError.toSeekError(): SeekError = when (this) {
            is ChannelPositionError.ClosedChannel -> BadFileDescriptor(message)
            is ChannelPositionError.InvalidArgument -> BadFileDescriptor(message)
            is ChannelPositionError.IoError -> BadFileDescriptor(message)
        }
    }
}
