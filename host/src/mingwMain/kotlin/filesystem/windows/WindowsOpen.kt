/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock.Companion.DIRECTORY_BASE_RIGHTS_BLOCK
import at.released.weh.filesystem.fdrights.getChildDirectoryRights
import at.released.weh.filesystem.fdrights.getChildFileRights
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.checkOpenFlags
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.fdresource.WindowsFileFdResource.WindowsFileChannel
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.nativefunc.open.FileDirectoryHandle
import at.released.weh.filesystem.windows.nativefunc.open.FileDirectoryHandle.Directory
import at.released.weh.filesystem.windows.nativefunc.open.FileDirectoryHandle.File
import at.released.weh.filesystem.windows.nativefunc.open.windowsOpenFileOrDirectory

internal class WindowsOpen(
    private val fsState: WindowsFileSystemState,
    private val pathResolver: WindowsPathResolver = fsState.pathResolver,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    @Suppress("ReturnCount")
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> {
        checkOpenFlags(input.openFlags, input.rights, input.path.isDirectoryRequest())
            .getOrElse { return it.left() }

        val directoryChannel: WindowsDirectoryChannel? = pathResolver.getBaseDirectory(input.baseDirectory)
            .getOrElse { return it.toResolveRelativePathErrors().left() }

        val baseDirectoryRights = directoryChannel?.rights ?: DIRECTORY_BASE_RIGHTS_BLOCK

        val path = pathResolver.getPath(input.baseDirectory, input.path)
            .getOrElse { return it.toResolveRelativePathErrors().left() }

        return windowsOpenFileOrDirectory(
            path = path,
            withRootAccess = pathResolver.withRootAccess,
            flags = input.openFlags,
            fdFlags = input.fdFlags,
        ).flatMap { nativeChannel: FileDirectoryHandle ->
            when (nativeChannel) {
                is File -> fsState.addFile(
                    WindowsFileChannel(
                        handle = nativeChannel.handle,
                        isInAppendMode = nativeChannel.isInAppendMode,
                        rights = baseDirectoryRights.getChildFileRights(input.rights),
                    ),
                )

                is Directory -> fsState.addDirectory(
                    WindowsDirectoryChannel(
                        handle = nativeChannel.handle,
                        isPreopened = false,
                        rights = baseDirectoryRights.getChildDirectoryRights(input.rights),
                        virtualPath = input.path,
                    ),
                )
            }
        }.map { (fd: FileDescriptor, _) -> fd }
    }
}
