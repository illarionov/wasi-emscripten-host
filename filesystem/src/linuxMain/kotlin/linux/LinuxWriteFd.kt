/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.ReadWriteStrategy.DO_NOT_CHANGE_POSITION
import at.released.weh.filesystem.op.readwrite.WriteFd

internal class LinuxWriteFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<WriteFd, WriteError, ULong> {
    override fun invoke(input: WriteFd): Either<WriteError, ULong> = fsState.executeWithResource(input.fd) {
        when (input.strategy) {
            CHANGE_POSITION -> it.writeChangePosition(input.cIovecs)
            DO_NOT_CHANGE_POSITION -> it.writeDoNotChangePosition(input.cIovecs)
        }
    }
}
