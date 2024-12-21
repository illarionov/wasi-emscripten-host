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
import at.released.weh.filesystem.fdrights.getChildDirectoryRights
import at.released.weh.filesystem.fdrights.getChildFileRights
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.checkOpenFlags
import at.released.weh.filesystem.linux.fdresource.LinuxFileFdResource.NativeFileChannel
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.FileDirectoryFd
import at.released.weh.filesystem.linux.native.FileDirectoryFd.Directory
import at.released.weh.filesystem.linux.native.FileDirectoryFd.File
import at.released.weh.filesystem.linux.native.ResolveModeFlag
import at.released.weh.filesystem.linux.native.linuxOpenFileOrDirectory
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor
import at.released.weh.filesystem.posix.fdresource.PosixDirectoryChannel

internal class LinuxOpen(
    private val fsState: LinuxFileSystemState,
    private val fsExecutor: FileSystemActionExecutor = fsState.pathResolver,
    private val isRootAccessAllowed: Boolean = false,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> {
        checkOpenFlags(input.openFlags, input.rights, input.path.isDirectoryRequest()).onLeft { return it.left() }

        return fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, realBaseDirectory ->
            openFileOrDirectory(input, realBaseDirectory, input.path, realPath)
        }
    }

    private fun openFileOrDirectory(
        input: Open,
        nativeChannel: PosixDirectoryChannel,
        virtualPath: VirtualPath,
        realPath: PosixRealPath,
    ): Either<OpenError, FileDescriptor> {
        val baseDirectoryRights = nativeChannel.rights

        val resolveFlags = if (isRootAccessAllowed) {
            setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS)
        } else {
            setOf(ResolveModeFlag.RESOLVE_NO_MAGICLINKS, ResolveModeFlag.RESOLVE_BENEATH)
        }

        return linuxOpenFileOrDirectory(
            baseDirectoryFd = nativeChannel.nativeFd,
            path = realPath,
            flags = input.openFlags,
            fdFlags = input.fdFlags,
            mode = input.mode,
            resolveFlags = resolveFlags,
        ).flatMap { fileOrDirectory: FileDirectoryFd ->
            when (fileOrDirectory) {
                is File -> fsState.addFile(
                    NativeFileChannel(
                        fd = fileOrDirectory.fd,
                        isInAppendMode = fileOrDirectory.isInAppendMode,
                        rights = baseDirectoryRights.getChildFileRights(input.rights),
                    ),
                )

                is Directory -> {
                    val channel = PosixDirectoryChannel(
                        nativeFd = fileOrDirectory.fd,
                        isPreopened = false,
                        virtualPath = virtualPath,
                        rights = baseDirectoryRights.getChildDirectoryRights(input.rights),
                    )
                    fsState.addDirectory(channel)
                }
            }
        }.map { (fd: FileDescriptor, _) -> fd }
    }
}
