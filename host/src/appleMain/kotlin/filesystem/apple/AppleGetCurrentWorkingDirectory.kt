/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toGetCwdError
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryChannel
import at.released.weh.filesystem.posix.fdresource.PosixPathResolver

internal class AppleGetCurrentWorkingDirectory(
    private val pathResolver: PosixPathResolver,
) : FileSystemOperationHandler<GetCurrentWorkingDirectory, GetCurrentWorkingDirectoryError, VirtualPath> {
    override fun invoke(input: GetCurrentWorkingDirectory): Either<GetCurrentWorkingDirectoryError, VirtualPath> {
        return pathResolver.getBaseDirectory(CurrentWorkingDirectory)
            .mapLeft<GetCurrentWorkingDirectoryError>(ResolvePathError::toGetCwdError)
            .map { cwdFd: PosixDirectoryChannel -> cwdFd.virtualPath }
    }
}
