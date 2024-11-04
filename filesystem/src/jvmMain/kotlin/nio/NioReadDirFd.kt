/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.fdresource.NioDirectoryFdResource
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.readdir.NioDirEntrySequence
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition.Cookie
import at.released.weh.filesystem.op.readdir.ReadDirFd.DirSequenceStartPosition.Start
import kotlinx.io.IOException
import java.nio.file.Files
import java.nio.file.NotDirectoryException

internal class NioReadDirFd(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<ReadDirFd, ReadDirError, DirEntrySequence> {
    override fun invoke(input: ReadDirFd): Either<ReadDirError, DirEntrySequence> {
        return fsState.executeWithResource(input.fd) { resource ->
            if (resource !is NioDirectoryFdResource) {
                return@executeWithResource BadFileDescriptor("${input.fd} is not a directory").left()
            }
            val rootPath = resource.path
            val stream = Either.catch { Files.newDirectoryStream(rootPath) }
                .mapLeft { it.toReadDirError() }
                .getOrElse {
                    return@executeWithResource it.left()
                }

            val cookie = when (val position = input.startPosition) {
                Start -> 0
                is Cookie -> position.cookie
            }

            NioDirEntrySequence(rootPath, stream, cookie).right()
        }
    }

    private companion object {
        internal fun Throwable.toReadDirError(): ReadDirError = when {
            this is NotDirectoryException -> NotDirectory("Path is not a directory")
            this is IOException -> IoError("Error: ${this.message}")
            else -> BadFileDescriptor("Error: ${this.message}")
        }
    }
}
