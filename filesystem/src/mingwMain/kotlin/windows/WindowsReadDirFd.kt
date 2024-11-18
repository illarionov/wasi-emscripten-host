/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState

internal class WindowsReadDirFd(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<ReadDirFd, ReadDirError, DirEntrySequence> {
    override fun invoke(input: ReadDirFd): Either<ReadDirError, DirEntrySequence> {
        return fsState.executeWithResource(input.fd) { resource ->
            if (resource !is WindowsDirectoryFdResource) {
                return@executeWithResource BadFileDescriptor("${input.fd} is not a directory").left()
            }
            TODO()
        }
    }
}
