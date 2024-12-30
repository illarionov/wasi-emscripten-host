/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.stdio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.model.FileSystemErrno.SUCCESS
import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.filesystem.stdio.StdioPollEvent.Companion.STDIO_POLL_EVENT_WRITE_SUCCESS
import kotlinx.io.RawSource
import kotlinx.io.asSource
import java.io.IOException
import java.io.InputStream

internal class InputStreamSourceProvider(
    private val streamProvider: () -> InputStream,
) : StdioSource.Provider {
    override fun open(): StdioSource = InputStreamStdioSource(streamProvider())
}

private class InputStreamStdioSource(
    private val inputStream: InputStream,
    source: RawSource = inputStream.asSource(),
) : StdioSource, RawSource by source {
    override fun pollNonblocking(type: FileDescriptorEventType): Either<NonblockingPollError, StdioPollEvent> {
        if (type == FileDescriptorEventType.WRITE) {
            return STDIO_POLL_EVENT_WRITE_SUCCESS.right()
        }
        return try {
            val bytesAvailable = inputStream.available()
            if (bytesAvailable != 0) {
                StdioPollEvent(
                    errno = SUCCESS,
                    type = FileDescriptorEventType.READ,
                    bytesAvailable = bytesAvailable.toLong(),
                    isHangup = false,
                ).right()
            } else {
                AGAIN_ERROR
            }
        } catch (ioe: IOException) {
            BadFileDescriptor("Input stream closed (${ioe.message})").left()
        }
    }

    private companion object {
        private val AGAIN_ERROR = Again("No data available").left()
    }
}
