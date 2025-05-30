/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxReadLink
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.posix.PosixPathConverter.toVirtualPath
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxReadLink(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<ReadLink, ReadLinkError, VirtualPath> {
    override fun invoke(input: ReadLink): Either<ReadLinkError, VirtualPath> {
        return fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            false,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, realBaseDirectory, _ ->
            linuxReadLink(realBaseDirectory.nativeFd, realPath)
        }.flatMap { targetRealPath: PosixRealPath ->
            toVirtualPath(targetRealPath).mapLeft<ResolveRelativePathErrors>(PathError::toResolveRelativePathErrors)
        }
    }
}
