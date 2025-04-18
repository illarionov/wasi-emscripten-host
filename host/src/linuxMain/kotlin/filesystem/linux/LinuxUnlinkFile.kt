/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxUnlinkFile
import at.released.weh.filesystem.op.unlink.UnlinkFile
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxUnlinkFile(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<UnlinkFile, UnlinkError, Unit> {
    override fun invoke(input: UnlinkFile): Either<UnlinkError, Unit> {
        return fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            false,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, realBaseDirectory, _ ->
            linuxUnlinkFile(realBaseDirectory.nativeFd, realPath)
        }
    }
}
