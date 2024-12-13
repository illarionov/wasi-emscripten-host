/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.chmod.ChmodFd

internal class LinuxChmodFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<ChmodFd, ChmodError, Unit> {
    override fun invoke(input: ChmodFd): Either<ChmodError, Unit> {
        return fsState.executeWithResource(input.fd) { fdResource ->
            fdResource.chmod(input.mode)
        }
    }
}
