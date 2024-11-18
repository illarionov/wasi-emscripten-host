/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.stat.StatFd
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsStatFd(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<StatFd, StatError, StructStat> {
    override fun invoke(input: StatFd): Either<StatError, StructStat> = fsState.executeWithResource(input.fd) {
        it.stat()
    }
}
