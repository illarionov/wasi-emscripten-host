/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.apple.ext.posixFd
import at.released.weh.filesystem.apple.readdir.AppleReadDirSequence
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryFdResource
import at.released.weh.filesystem.posix.nativefunc.PosixDupfdMapper.dupErrorToReadDirError
import at.released.weh.filesystem.posix.readdir.posixOpenDir
import platform.posix.dup
import platform.posix.errno

internal class AppleReadDirFd(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<ReadDirFd, ReadDirError, DirEntrySequence> {
    override fun invoke(input: ReadDirFd): Either<ReadDirError, DirEntrySequence> {
        return fsState.executeWithResource(input.fd) { resource ->
            if (resource !is PosixDirectoryFdResource) {
                return@executeWithResource BadFileDescriptor("${input.fd} is not a directory").left()
            }

            // need a dup since closedir() closes the underlying file descriptor
            val dirFdDup = dup(resource.channel.nativeFd.posixFd)
            if (dirFdDup == -1) {
                return@executeWithResource dupErrorToReadDirError(errno).left()
            }

            posixOpenDir(NativeDirectoryFd(dirFdDup)).map { dirPointer ->
                AppleReadDirSequence(resource.channel.virtualPath, dirPointer, input.startPosition)
            }
        }
    }
}
