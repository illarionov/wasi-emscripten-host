/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.open

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_ONLY
import platform.windows.HANDLE

internal fun <E : FileSystemOperationError, R : Any> WindowsFileSystemState.executeWithOpenFileHandle(
    baseDirectory: BaseDirectory,
    path: RealPath,
    followSymlinks: Boolean = true,
    access: AttributeDesiredAccess = READ_ONLY,
    errorMapper: (OpenError) -> E,
    block: (HANDLE) -> Either<E, R>,
): Either<E, R> {
    val directoryFd: WindowsDirectoryChannel? = pathResolver.resolveBaseDirectory(baseDirectory)
        .mapLeft { errorMapper(it as OpenError) }
        .getOrElse {
            return it.left()
        }

    return useFileForAttributeAccess(
        baseHandle = directoryFd?.handle,
        path = path,
        followSymlinks = followSymlinks,
        access = access,
        errorMapper = errorMapper,
        block = block,
    )
}
