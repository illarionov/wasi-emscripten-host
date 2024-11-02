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
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.checkOpenFlags
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.FileDirectoryFd.Directory
import at.released.weh.filesystem.linux.native.FileDirectoryFd.File
import at.released.weh.filesystem.linux.native.ResolveModeFlag
import at.released.weh.filesystem.linux.native.linuxOpenFileOrDirectory
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
            linuxOpenFileOrDirectory(
                baseDirectoryFd = directoryFd,
                path = input.path,
                flags = input.openFlags,
                fdFlags = input.fdFlags,
                mode = input.mode,
                resolveFlags = resolveFlags,
            ).flatMap { nativeChannel ->
                when (nativeChannel) {
                    is File -> fsState.addFile(nativeChannel.channel)
                    is Directory -> fsState.addDirectory(
                        nativeFd = nativeChannel.fd,
                        virtualPath = input.path, // XXX: check
                    )
                }
            }.map { (fd: FileDescriptor, _) -> fd }
        }
    }
}
