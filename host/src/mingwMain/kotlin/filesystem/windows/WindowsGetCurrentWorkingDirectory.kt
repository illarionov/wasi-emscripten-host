/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NameTooLong
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import kotlinx.cinterop.ByteVarOf
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.EACCES
import platform.posix.EINVAL
import platform.posix.ENAMETOOLONG
import platform.posix.ENOENT
import platform.posix.PATH_MAX
import platform.posix.errno
import platform.posix.getcwd

internal class WindowsGetCurrentWorkingDirectory :
    FileSystemOperationHandler<GetCurrentWorkingDirectory, GetCurrentWorkingDirectoryError, VirtualPath> {
    override fun invoke(input: GetCurrentWorkingDirectory): Either<GetCurrentWorkingDirectoryError, VirtualPath> {
        val byteArray = ByteArray(PATH_MAX)
        // TODO: change
        val cwd: CPointer<ByteVarOf<Byte>>? = byteArray.usePinned { bytes ->
            getcwd(bytes.addressOf(0), PATH_MAX)
        }
        return if (cwd != null) {
            WindowsRealPath.create(byteArray.decodeToString())
                .flatMap { realPath -> WindowsPathConverter.toVirtualPath(realPath) }
                .mapLeft { error -> InvalidArgument(error.message) }
        } else {
            errno.errnoToGetCurrentWorkingDirectoryError().left()
        }
    }

    private fun Int.errnoToGetCurrentWorkingDirectoryError(): GetCurrentWorkingDirectoryError = when (this) {
        EACCES -> AccessDenied("Access denied")
        EINVAL -> InvalidArgument("Invalid argument")
        ENAMETOOLONG -> NameTooLong("Current working directory name too long")
        ENOENT -> NoEntry("Current working directory has been unlinked")
        else -> InvalidArgument("Unexpected error `$this`")
    }
}
