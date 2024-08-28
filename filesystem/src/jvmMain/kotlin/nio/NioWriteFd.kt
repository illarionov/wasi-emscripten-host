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
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.ext.asByteBuffer
import at.released.weh.filesystem.ext.writeCatching
import at.released.weh.filesystem.fd.NioFileHandle
import at.released.weh.filesystem.fd.getPosition
import at.released.weh.filesystem.internal.ChannelPositionError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.Messages.fileDescriptorNotOpenedMessage
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.WriteFd
import kotlin.concurrent.withLock
import at.released.weh.filesystem.error.InvalidArgument as BaseInvalidArgument
import at.released.weh.filesystem.error.IoError as BaseIoError

internal class NioWriteFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<WriteFd, WriteError, ULong> {
    override fun invoke(input: WriteFd): Either<WriteError, ULong> = fsState.fsLock.withLock {
        val channel = fsState.fileDescriptors.get(input.fd)
            ?: return BadFileDescriptor(fileDescriptorNotOpenedMessage(input.fd)).left()
        return when (input.strategy) {
            DO_NOT_CHANGE_POSITION -> writeDoNotChangePosition(channel, input.cIovecs)
            CHANGE_POSITION -> writeChangePosition(channel, input.cIovecs)
        }
    }

    private fun writeDoNotChangePosition(
        channel: NioFileHandle,
        cIovecs: List<FileSystemByteBuffer>,
    ): Either<WriteError, ULong> = either {
        var position = channel.getPosition()
            .mapLeft { it.toWriteError() }
            .bind()

        var totalBytesWritten = 0UL
        for (ciovec in cIovecs) {
            val byteBuffer = ciovec.asByteBuffer()
            val bytesWritten = writeCatching {
                channel.channel.write(byteBuffer, position)
            }.bind()
            if (bytesWritten > 0) {
                position += bytesWritten
                totalBytesWritten += bytesWritten.toULong()
            }
            if (bytesWritten < byteBuffer.limit()) {
                break
            }
        }
        totalBytesWritten
    }

    private fun writeChangePosition(
        channel: NioFileHandle,
        cIovecs: List<FileSystemByteBuffer>,
    ): Either<WriteError, ULong> {
        val byteBuffers = Array(cIovecs.size) { cIovecs[it].asByteBuffer() }
        return writeCatching {
            channel.channel.write(byteBuffers).toULong()
        }
    }

    private companion object {
        private fun ChannelPositionError.toWriteError(): WriteError = when (this) {
            is ChannelPositionError.ClosedChannel -> BaseIoError(message)
            is ChannelPositionError.InvalidArgument -> BaseInvalidArgument(message)
            is ChannelPositionError.IoError -> BaseIoError(message)
        }
    }
}
