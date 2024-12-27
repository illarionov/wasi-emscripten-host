/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxSymlink
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.posix.PosixPathConverter.toRealPath
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.withPathErrorAsCommonError
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor
import at.released.weh.filesystem.posix.validateSymlinkTarget

internal class LinuxSymlink(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<Symlink, SymlinkError, Unit> {
    override fun invoke(input: Symlink): Either<SymlinkError, Unit> {
        return validateSymlinkTarget(input.oldPath, input.allowAbsoluteOldPath)
            .flatMap { toRealPath(input.oldPath).withPathErrorAsCommonError() }
            .flatMap { oldRealpath: PosixRealPath ->
                fsExecutor.executeWithPath(
                    input.newPath,
                    input.newPathBaseDirectory,
                    false,
                    ResolvePathError::toResolveRelativePathErrors,
                ) { newRealPath, directoryFd, _ ->
                    linuxSymlink(oldRealpath, newRealPath, directoryFd.nativeFd)
                }
            }
    }
}
