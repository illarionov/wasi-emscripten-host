/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.ext.linuxFd
import at.released.weh.filesystem.linux.fdresource.LinuxDirectoryFdResource
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.op.readdir.DirEntrySequence
import at.released.weh.filesystem.op.readdir.ReadDirFd
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.nativefunc.PosixDupfdMapper.dupErrorToReadDirError
import at.released.weh.filesystem.posix.readdir.PosixDirEntrySequence
import at.released.weh.filesystem.posix.readdir.posixOpenDir
import platform.posix.dup
import platform.posix.errno

internal class LinuxReadDirFd(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<ReadDirFd, ReadDirError, DirEntrySequence> {
    override fun invoke(input: ReadDirFd): Either<ReadDirError, DirEntrySequence> {
        return fsState.executeWithResource(input.fd) { resource ->
            if (resource !is LinuxDirectoryFdResource) {
                return@executeWithResource BadFileDescriptor("${input.fd} is not a directory").left()
            }

            // need a dup since closedir() closes the underlying file descriptor
            val dirFdDup = dup(resource.nativeFd.linuxFd)
            if (dirFdDup == -1) {
                return@executeWithResource dupErrorToReadDirError(errno).left()
            }

            posixOpenDir(NativeDirectoryFd(dirFdDup)).map { dirPointer ->
                PosixDirEntrySequence(resource.virtualPath, dirPointer, input.startPosition)
            }
        }
    }
}
