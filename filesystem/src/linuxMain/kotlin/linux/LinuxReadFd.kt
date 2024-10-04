/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.readwrite.ReadFd
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION

internal class LinuxReadFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<ReadFd, ReadError, ULong> {
    override fun invoke(input: ReadFd): Either<ReadError, ULong> = fsState.executeWithResource(input.fd) {
        when (input.strategy) {
            CHANGE_POSITION -> it.readChangePosition(input.iovecs)
            DO_NOT_CHANGE_POSITION -> it.readDoNotChangePosition(input.iovecs)
        }
    }
}
