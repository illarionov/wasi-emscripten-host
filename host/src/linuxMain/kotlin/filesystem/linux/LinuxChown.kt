/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxChown
import at.released.weh.filesystem.op.chown.Chown
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxChown(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<Chown, ChownError, Unit> {
    override fun invoke(input: Chown): Either<ChownError, Unit> =
        fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            input.followSymlinks,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, realBaseDirectory, nativeFollowSymlinks ->
            linuxChown(realBaseDirectory.nativeFd, realPath, input.owner, input.group, nativeFollowSymlinks)
        }
}
