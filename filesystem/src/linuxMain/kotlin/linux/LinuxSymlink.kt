/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.SymlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.linuxSymlink
import at.released.weh.filesystem.op.symlink.Symlink
import at.released.weh.filesystem.path.PosixPathConverter.convertToRealPath
import at.released.weh.filesystem.path.PosixPathConverter.toRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.posix.validateSymlinkTarget

internal class LinuxSymlink(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<Symlink, SymlinkError, Unit> {
    override fun invoke(input: Symlink): Either<SymlinkError, Unit> {
        return VirtualPath.of(input.oldPath)
            .mapLeft { InvalidArgument(it.message) }
            .flatMap { path ->
                validateSymlinkTarget(path, input.allowAbsoluteOldPath).map { path }
            }
            .flatMap { toRealPath(it) }
            .flatMap { oldRealpath ->
                convertToRealPath(input.newPath).map { oldRealpath to it }
            }.flatMap { (oldRealpath, newRealPath) ->
                fsState.executeWithBaseDirectoryResource(input.newPathBaseDirectory) { directoryFd ->
                    linuxSymlink(oldRealpath, newRealPath, directoryFd)
                }
            }
    }
}
