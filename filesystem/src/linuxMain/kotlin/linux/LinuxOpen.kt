/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock.Companion.DIRECTORY_BASE_RIGHTS_BLOCK
import at.released.weh.filesystem.fdrights.getChildDirectoryRights
import at.released.weh.filesystem.fdrights.getChildFileRights
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.checkOpenFlags
import at.released.weh.filesystem.linux.fdresource.LinuxDirectoryFdResource
import at.released.weh.filesystem.linux.fdresource.LinuxFileFdResource.NativeFileChannel
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.FileDirectoryFd.Directory
import at.released.weh.filesystem.linux.native.FileDirectoryFd.File
import at.released.weh.filesystem.linux.native.ResolveModeFlag
import at.released.weh.filesystem.linux.native.linuxOpenFileOrDirectory
import at.released.weh.filesystem.model.BaseDirectory.DirectoryFd
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.opencreate.Open

internal class LinuxOpen(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> {
        val isDirectoryRequest = input.path.endsWith("/")
        checkOpenFlags(input.openFlags, input.rights, isDirectoryRequest).onLeft { return it.left() }

        val resolveFlags = if (fsState.isRootAccessAllowed) {
            setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS)
        } else {
            setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS, ResolveModeFlag.RESOLVE_BENEATH)
        }

        return fsState.executeWithBaseDirectoryResource(input.baseDirectory) { directoryFd ->
            val baseDirectoryRights = (input.baseDirectory as? DirectoryFd)?.let { baseDirectoryFd ->
                (fsState.get(baseDirectoryFd.fd) as? LinuxDirectoryFdResource)?.rights
            } ?: DIRECTORY_BASE_RIGHTS_BLOCK

            linuxOpenFileOrDirectory(
                baseDirectoryFd = directoryFd,
                path = input.path,
                flags = input.openFlags,
                fdFlags = input.fdFlags,
                mode = input.mode,
                resolveFlags = resolveFlags,
            ).flatMap { nativeChannel ->
                when (nativeChannel) {
                    is File -> fsState.addFile(
                        NativeFileChannel(
                            fd = nativeChannel.fd,
                            isInAppendMode = nativeChannel.isInAppendMode,
                            rights = baseDirectoryRights.getChildFileRights(input.rights),
                        ),
                    )

                    is Directory -> fsState.addDirectory(
                        nativeFd = nativeChannel.fd,
                        virtualPath = input.path, // XXX: check
                        rights = baseDirectoryRights.getChildDirectoryRights(input.rights),
                    )
                }
            }.map { (fd: FileDescriptor, _) -> fd }
        }
    }
}
