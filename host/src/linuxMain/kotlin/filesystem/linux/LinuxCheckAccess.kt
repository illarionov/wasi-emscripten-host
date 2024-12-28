/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxCheckAccess
import at.released.weh.filesystem.op.checkaccess.CheckAccess
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxCheckAccess(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<CheckAccess, CheckAccessError, Unit> {
    override fun invoke(input: CheckAccess): Either<CheckAccessError, Unit> =
        fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            input.followSymlinks,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, realBaseDirectory, nativeFollowSymlinks ->
            linuxCheckAccess(
                path = realPath,
                baseDirectoryFd = realBaseDirectory.nativeFd,
                mode = input.mode,
                useEffectiveUserId = input.useEffectiveUserId,
                followSymlinks = nativeFollowSymlinks,
            )
        }
}
