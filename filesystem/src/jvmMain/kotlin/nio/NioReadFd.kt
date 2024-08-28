/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.ext.asByteBuffer
import at.released.weh.filesystem.ext.readCatching
import at.released.weh.filesystem.fd.getPosition
import at.released.weh.filesystem.internal.ChannelPositionError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenedMessage
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION
import kotlin.concurrent.withLock
import at.released.weh.filesystem.error.InvalidArgument as FileSystemOperationInvalidArgument
import at.released.weh.filesystem.error.IoError as FileSystemOperationIoError

internal class NioReadFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<ReadFd, ReadError, ULong> {
    override fun invoke(input: ReadFd): Either<ReadError, ULong> = fsState.fsLock.withLock {
        val channel = fsState.fileDescriptors.get(input.fd)
            ?: return BadFileDescriptor(fileDescriptorNotOpenedMessage(input.fd)).left()
        return when (input.strategy) {
            DO_NOT_CHANGE_POSITION -> readDoNotChangePosition(channel, input.iovecs)
            CHANGE_POSITION -> readChangePosition(channel, input.iovecs)
        }
    }

    private fun readDoNotChangePosition(
        channel: at.released.weh.filesystem.fd.NioFileHandle,
        iovecs: List<FileSystemByteBuffer>,
    ): Either<ReadError, ULong> = either {
        var position = channel.getPosition()
            .mapLeft { it.toReadError() }
            .bind()

        var totalBytesRead: ULong = 0U
        for (iovec in iovecs) {
            val byteBuffer = iovec.asByteBuffer()
            val bytesRead = readCatching {
                channel.channel.read(byteBuffer, position)
            }.bind()
            if (bytesRead > 0) {
                position += bytesRead
                totalBytesRead += bytesRead.toULong()
            }
            if (bytesRead < byteBuffer.limit()) {
                break
            }
        }
        totalBytesRead
    }

    private fun readChangePosition(
        channel: at.released.weh.filesystem.fd.NioFileHandle,
        iovecs: List<FileSystemByteBuffer>,
    ): Either<ReadError, ULong> {
        val byteBuffers = Array(iovecs.size) { iovecs[it].asByteBuffer() }
        val bytesRead: Either<ReadError, Long> = readCatching {
            channel.channel.read(byteBuffers)
        }
        return bytesRead.map {
            if (it != -1L) it.toULong() else 0UL
        }
    }

    private companion object {
        private fun ChannelPositionError.toReadError(): ReadError = when (this) {
            is ChannelPositionError.ClosedChannel -> FileSystemOperationIoError(message)
            is ChannelPositionError.InvalidArgument -> FileSystemOperationInvalidArgument(message)
            is ChannelPositionError.IoError -> FileSystemOperationIoError(message)
        }
    }
}
