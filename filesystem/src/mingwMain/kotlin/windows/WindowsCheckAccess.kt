/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.nativefunc.open.executeWithOpenFileHandle
import at.released.weh.filesystem.windows.nativefunc.windowsCheckAccessFd
import platform.windows.HANDLE

internal class WindowsCheckAccess(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<CheckAccess, CheckAccessError, Unit> {
    override fun invoke(input: CheckAccess): Either<CheckAccessError, Unit> {
        return fsState.executeWithOpenFileHandle(
            baseDirectory = input.baseDirectory,
            path = input.path,
            followSymlinks = input.followSymlinks,
            writeAccess = false,
            errorMapper = { it.toCheckAccessError() },
        ) { handle: HANDLE -> windowsCheckAccessFd(handle, input.mode, input.useEffectiveUserId) }
    }

    private fun OpenError.toCheckAccessError(): CheckAccessError = if (this is CheckAccessError) {
        this
    } else {
        IoError(this.message)
    }
}
