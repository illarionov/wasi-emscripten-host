/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.PrestatError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.prestat.PrestatFd
import at.released.weh.filesystem.op.prestat.PrestatResult
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryFdResource

internal class ApplePrestatFd(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<PrestatFd, PrestatError, PrestatResult> {
    override fun invoke(input: PrestatFd): Either<PrestatError, PrestatResult> {
        val resource = fsState.get(input.fd) as? PosixDirectoryFdResource
        val path = if (resource?.channel?.isPreopened == true) {
            resource.channel.virtualPath
        } else {
            null
        }
        return if (path != null) {
            PrestatResult(input.fd, path).right()
        } else {
            BadFileDescriptor("${input.fd} is not a preopened directory").left()
        }
    }
}
