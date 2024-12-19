/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.fdresource

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.path.real.windows.WindowsPathType
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.preopened.BatchDirectoryOpener
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.path.NtPath
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtOpenDirectory
import at.released.weh.filesystem.windows.win32api.windowsDosPathNameToNtPathName
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_TRAVERSE
import platform.windows.FILE_WRITE_ATTRIBUTES

internal object WindowsBatchDirectoryOpener : BatchDirectoryOpener<WindowsRealPath, WindowsDirectoryChannel>(
    WindowsRealPath,
) {
    override fun preopenDirectory(
        path: WindowsRealPath,
        virtualPath: VirtualPath,
        baseDirectoryFd: WindowsDirectoryChannel?,
    ): Either<OpenError, WindowsDirectoryChannel> {
        val cwdHandle = baseDirectoryFd?.handle

        val ntPath = when {
            path.type == WindowsPathType.RELATIVE && cwdHandle != null -> {
                // Resolve relative to cwdHandle
                WindowsNtRelativePath.createFromRelativeWindowsPath(path)
                    .map { NtPath.Relative(cwdHandle, it) }
            }

            else -> windowsDosPathNameToNtPathName(path.kString).map { NtPath.Absolute(it) }
        }.getOrElse { return it.toResolveRelativePathErrors().left() }

        return windowsNtOpenDirectory(
            ntPath = ntPath,
            desiredAccess = FILE_LIST_DIRECTORY or
                    FILE_READ_ATTRIBUTES or
                    FILE_TRAVERSE or
                    FILE_WRITE_ATTRIBUTES,
        ).map { newHandle ->
            WindowsDirectoryChannel(
                handle = newHandle,
                isPreopened = true,
                rights = FdRightsBlock.DIRECTORY_BASE_RIGHTS_BLOCK,
                virtualPath = virtualPath,
            )
        }
    }

    override fun closeResource(resource: WindowsDirectoryChannel): Either<CloseError, Unit> {
        return resource.handle.close().onLeft {
            // IGNORE
        }
    }
}
