/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.apple.nativefunc.appleSymlink
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.path.PosixPathConverter.toRealPath
import at.released.weh.filesystem.posix.validateSymlinkTarget

internal class AppleSymlink(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<Symlink, SymlinkError, Unit> {
    override fun invoke(input: Symlink): Either<SymlinkError, Unit> {
        return validateSymlinkTarget(input.oldPath, input.allowAbsoluteOldPath)
            .flatMap { toRealPath(input.oldPath) }
            .flatMap { oldRealpath ->
                fsState.executeWithPath(input.newPath, input.newPathBaseDirectory) { newRealPath, directoryFd ->
                    appleSymlink(oldRealpath, newRealPath, directoryFd)
                }
            }
    }
}
