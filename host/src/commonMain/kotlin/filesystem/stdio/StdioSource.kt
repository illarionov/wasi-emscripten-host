/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.stdio

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.filesystem.op.poll.FileDescriptorEventType.READ
import at.released.weh.filesystem.op.poll.FileDescriptorEventType.WRITE
import at.released.weh.filesystem.stdio.StdioPollEvent.Companion.STDIO_POLL_EVENT_READ_SUCCESS
import at.released.weh.filesystem.stdio.StdioPollEvent.Companion.STDIO_POLL_EVENT_WRITE_SUCCESS
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource

public interface StdioSource : RawSource {
    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long
    public fun pollNonblocking(
        type: FileDescriptorEventType,
    ): Either<NonblockingPollError, StdioPollEvent> = when (type) {
        READ -> STDIO_POLL_EVENT_READ_SUCCESS
        WRITE -> STDIO_POLL_EVENT_WRITE_SUCCESS
    }.right()
    override fun close()

    public fun interface Provider {
        @Throws(IOException::class)
        public fun open(): StdioSource
    }
}
