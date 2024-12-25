/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.wasi.preview1.type.Event
import at.released.weh.wasi.preview1.type.EventFdReadwrite
import at.released.weh.wasi.preview1.type.EventrwflagsFlag.FD_READWRITE_HANGUP
import at.released.weh.wasi.preview1.type.Eventtype
import kotlinx.io.Sink
import kotlinx.io.writeIntLe
import kotlinx.io.writeLongLe
import kotlinx.io.writeShortLe
import at.released.weh.filesystem.op.poll.Event as FileSystemEvent

internal object EventMapper {
    internal const val EVENT_PACKED_SIZE = 32
    internal const val EVENT_FD_READWRITE_PACKED_SIZE = 16
    private val DUMMY_EVENT_FD_READWRITE = EventFdReadwrite(0, 0)

    internal fun Event.packTo(
        sink: Sink,
    ) {
        sink.writeLongLe(this.userdata)
        sink.writeShortLe(this.error.code.toShort())
        sink.writeByte(this.type.code.toByte())
        sink.writeByte(0) // alignment
        sink.writeIntLe(0) // alignment

        when (type) {
            Eventtype.CLOCK -> DUMMY_EVENT_FD_READWRITE.packTo(sink)
            Eventtype.FD_READ, Eventtype.FD_WRITE -> fdReadwrite.packTo(sink)
        }
    }

    private fun EventFdReadwrite.packTo(
        sink: Sink,
    ) {
        sink.writeLongLe(nbytes)
        sink.writeShortLe(flags)
        sink.writeShortLe(0) // alignment
        sink.writeIntLe(0) // alignment
    }

    internal fun fromFilesystemEvent(event: FileSystemEvent): Event = when (event) {
        is FileSystemEvent.ClockEvent -> Event(
            userdata = event.userdata,
            error = event.errno.toWasiErrno(),
            type = Eventtype.CLOCK,
            fdReadwrite = DUMMY_EVENT_FD_READWRITE,
        )

        is FileSystemEvent.FileDescriptorEvent -> Event(
            userdata = event.userdata,
            error = event.errno.toWasiErrno(),
            type = when (event.type) {
                FileDescriptorEventType.READ -> Eventtype.FD_READ
                FileDescriptorEventType.WRITE -> Eventtype.FD_WRITE
            },
            fdReadwrite = EventFdReadwrite(
                nbytes = event.bytesAvailable,
                flags = if (event.isHangup) {
                    FD_READWRITE_HANGUP
                } else {
                    0.toShort()
                },
            ),
        )
    }
}
