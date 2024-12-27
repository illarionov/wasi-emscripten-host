/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.apple.fdresource.AppleFileFdResource.NativeFileChannel
import at.released.weh.filesystem.apple.nativefunc.FileDirectoryFd
import at.released.weh.filesystem.apple.nativefunc.appleOpenFileOrDirectory
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.getChildDirectoryRights
import at.released.weh.filesystem.fdrights.getChildFileRights
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.checkOpenFlags
import at.released.weh.filesystem.internal.op.openFileFlagsWithFollowSymlinks
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOFOLLOW
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryChannel

internal class AppleOpen(
    private val fsState: AppleFileSystemState,
    private val fsExecutor: FileSystemActionExecutor = fsState.pathResolver,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> {
        checkOpenFlags(input.openFlags, input.rights, input.path.isDirectoryRequest()).onLeft { return it.left() }

        val followSymlinks = input.openFlags and O_NOFOLLOW != O_NOFOLLOW

        return fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            followSymlinks,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, baseDirectory: PosixDirectoryChannel, nativeFollowSymlinks ->
            val baseDirectoryRights = baseDirectory.rights
            appleOpenFileOrDirectory(
                baseDirectoryFd = baseDirectory.nativeFd,
                path = realPath,
                flags = openFileFlagsWithFollowSymlinks(input.openFlags, nativeFollowSymlinks),
                fdFlags = input.fdFlags,
                mode = input.mode,
            ).flatMap { fileOrDirectory: FileDirectoryFd ->
                when (fileOrDirectory) {
                    is FileDirectoryFd.File -> fsState.addFile(
                        NativeFileChannel(
                            fd = fileOrDirectory.fd,
                            rights = baseDirectoryRights.getChildFileRights(input.rights),
                        ),
                    )

                    is FileDirectoryFd.Directory -> {
                        val channel = PosixDirectoryChannel(
                            nativeFd = fileOrDirectory.fd,
                            isPreopened = false,
                            virtualPath = input.path,
                            rights = baseDirectoryRights.getChildDirectoryRights(input.rights),
                        )
                        fsState.addDirectory(channel)
                    }
                }
            }.map { (fd: FileDescriptor, _) -> fd }
        }
    }
}
