/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxChmod
import at.released.weh.filesystem.op.chmod.Chmod
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxChmod(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<Chmod, ChmodError, Unit> {
    override fun invoke(input: Chmod): Either<ChmodError, Unit> =
        fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            input.followSymlinks,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, realBaseDirectory, nativeFollowSymlinks ->
            linuxChmod(realBaseDirectory.nativeFd, realPath, input.mode, nativeFollowSymlinks)
        }
}
