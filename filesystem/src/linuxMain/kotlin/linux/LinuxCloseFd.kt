/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.linux.fdresource.LinuxDirectoryFdResource
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.close.CloseFd

internal class LinuxCloseFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<CloseFd, CloseError, Unit> {
    override fun invoke(input: CloseFd): Either<CloseError, Unit> {
        return fsState.executeWithResource(input.fd) { fdResource ->
            if (fdResource is LinuxDirectoryFdResource && fdResource.isPreopened) {
                // Preopened directory is not closeable while working with the file system
                BadFileDescriptor("Preopened directory is not closeable while working with the file system").left()
            } else {
                fsState.remove(input.fd).flatMap(FdResource::close)
            }
        }
    }
}
