/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.poll

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.SUCCESS

public sealed interface Event {
    public val errno: FileSystemErrno
    public val userdata: Long

    @WasiEmscriptenHostDataModel
    public class ClockEvent(
        public override val errno: FileSystemErrno = SUCCESS,
        public override val userdata: Long = 0,
    ) : Event

    @WasiEmscriptenHostDataModel
    public class FileDescriptorEvent(
        public override val errno: FileSystemErrno = SUCCESS,
        public override val userdata: Long = 0,
        public val fileDescriptor: FileDescriptor,
        public val type: FileDescriptorEventType,
        public val bytesAvailable: Long,
        public val isHangup: Boolean = false,
    ) : Event
}
