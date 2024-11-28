/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.open

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.posix.ext.validatePath
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtCreateFileEx
import kotlinx.io.IOException
import platform.windows.FILE_OPEN
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_WRITE_ATTRIBUTES
import platform.windows.HANDLE
import platform.windows.PathIsRelativeW

internal fun <E : FileSystemOperationError, R : Any> useFileForAttributeAccess(
    baseHandle: HANDLE?,
    path: RealPath,
    followSymlinks: Boolean = true,
    writeAccess: Boolean = true,
    errorMapper: (OpenError) -> E,
    block: (HANDLE) -> Either<E, R>,
): Either<E, R> {
    val handle: HANDLE = windowsOpenForAttributeAccess(baseHandle, path, followSymlinks, writeAccess)
        .mapLeft(errorMapper)
        .getOrElse { return it.left() }

    val blockResult = executeBlockSafe(handle, block)
    val closeResult = handle.close()

    return blockResult.fold(
        ifRight = { result: R ->
            if (closeResult.isRight()) {
                result.right()
            } else {
                errorMapper(closeResult.left() as OpenError).left()
            }
        },
        ifLeft = { throwableOrError: Either<Throwable, E> ->
            val blockFilesystemError = throwableOrError.getOrElse { throwable ->
                closeResult.leftOrNull()?.let { closeError ->
                    val closeException = IOException("Close() failed: $closeError")
                    throwable.addSuppressed(closeException)
                }
                throw throwable
            }
            blockFilesystemError.left()
        },
    )
}

private fun <E : FileSystemOperationError, R : Any> executeBlockSafe(
    handle: HANDLE,
    block: (HANDLE) -> Either<E, R>,
): Either<Either<Throwable, E>, R> = try {
    block(handle).mapLeft { it.right() }
} catch (@Suppress("TooGenericExceptionCaught") ex: Throwable) {
    ex.left().left()
}

internal fun windowsOpenForAttributeAccess(
    baseHandle: HANDLE?,
    path: RealPath,
    followSymlinks: Boolean = true,
    writeAccess: Boolean,
): Either<OpenError, HANDLE> = either {
    val rootHandle = if (baseHandle != null && PathIsRelativeW(path) != 0) {
        baseHandle
    } else {
        null
    }
    validatePath(path).bind()

    val desiredAccess = if (writeAccess) {
        FILE_READ_ATTRIBUTES or FILE_WRITE_ATTRIBUTES
    } else {
        FILE_READ_ATTRIBUTES
    }

    return windowsNtCreateFileEx(
        rootHandle = rootHandle,
        path = path,
        desiredAccess = desiredAccess,
        fileAttributes = 0,
        createDisposition = FILE_OPEN,
        createOptions = 0,
        followSymlinks = followSymlinks,
    )
}
