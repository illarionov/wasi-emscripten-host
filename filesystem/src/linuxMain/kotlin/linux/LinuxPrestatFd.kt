/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.PrestatError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxDirectoryFdResource
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.prestat.PrestatFd
import at.released.weh.filesystem.op.prestat.PrestatResult

internal class LinuxPrestatFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<PrestatFd, PrestatError, PrestatResult> {
    override fun invoke(input: PrestatFd): Either<PrestatError, PrestatResult> {
        val resource = fsState.get(input.fd) as? LinuxDirectoryFdResource
        val path = if (resource?.isPreopened == true) {
            resource.virtualPath
        } else {
            null
        }
        return if (path != null) {
            PrestatResult(input.fd, path).right()
        } else {
            BadFileDescriptor("${input.fd} is not a directory").left()
        }
    }
}
