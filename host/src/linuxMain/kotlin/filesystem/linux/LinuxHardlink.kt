/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.HardlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxHardlink
import at.released.weh.filesystem.op.hardlink.Hardlink
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxHardlink(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<Hardlink, HardlinkError, Unit> {
    override fun invoke(input: Hardlink): Either<HardlinkError, Unit> {
        return fsExecutor.executeWithPath(
            input.oldPath,
            input.oldBaseDirectory,
            input.followSymlinks,
            ResolvePathError::toResolveRelativePathErrors,
        ) { oldRealPath, oldRealBaseDirectory, _ ->
            fsExecutor.executeWithPath(
                input.newPath,
                input.newBaseDirectory,
                false,
                ResolvePathError::toResolveRelativePathErrors,
            ) { newRealPath, newRealBaseDirectory, nativeFollowSymlinks ->
                linuxHardlink(
                    oldRealBaseDirectory.nativeFd,
                    oldRealPath,
                    newRealBaseDirectory.nativeFd,
                    newRealPath,
                    nativeFollowSymlinks,
                )
            }
        }
    }
}
