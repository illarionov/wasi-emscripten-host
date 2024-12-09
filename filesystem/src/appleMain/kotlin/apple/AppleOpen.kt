/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.apple.fdresource.AppleDirectoryFdResource
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource.NativeFileChannel
import at.released.weh.filesystem.apple.nativefunc.FileDirectoryFd
import at.released.weh.filesystem.apple.nativefunc.appleOpenFileOrDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock.Companion.DIRECTORY_BASE_RIGHTS_BLOCK
import at.released.weh.filesystem.fdrights.getChildDirectoryRights
import at.released.weh.filesystem.fdrights.getChildFileRights
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.checkOpenFlags
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest

internal class AppleOpen(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> {
        checkOpenFlags(input.openFlags, input.rights, input.path.isDirectoryRequest()).onLeft { return it.left() }

        return fsState.executeWithPath(input.path, input.baseDirectory) { realPath, baseDirectory ->
            val baseDirectoryRights = (input.baseDirectory as? DirectoryFd)?.let { baseDirectoryFd ->
                (fsState.get(baseDirectoryFd.fd) as? AppleDirectoryFdResource)?.rights
            } ?: DIRECTORY_BASE_RIGHTS_BLOCK

            appleOpenFileOrDirectory(
                baseDirectoryFd = baseDirectory,
                path = realPath,
                flags = input.openFlags,
                fdFlags = input.fdFlags,
                mode = input.mode,
            ).flatMap { nativeChannel: FileDirectoryFd ->
                when (nativeChannel) {
                    is FileDirectoryFd.File -> fsState.addFile(
                        NativeFileChannel(
                            fd = nativeChannel.fd,
                            rights = baseDirectoryRights.getChildFileRights(input.rights),
                        ),
                    )

                    is FileDirectoryFd.Directory -> fsState.addDirectory(
                        nativeFd = nativeChannel.fd,
                        virtualPath = input.path,
                        rights = baseDirectoryRights.getChildDirectoryRights(input.rights),
                    )
                }
            }.map { (fd: FileDescriptor, _) -> fd }
        }
    }
}
