/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.fdresource

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.path.WindowsPathConverter.generatePreopenedDirectoryVirtualPath
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtOpenDirectory
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_TRAVERSE
import platform.windows.FILE_WRITE_ATTRIBUTES

internal fun preopenDirectories(
    currentWorkingDirectoryPath: RealPath = ".",
    preopenedDirectories: List<PreopenedDirectory> = listOf(),
): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
    val currentWorkingDirectory: Either<OpenError, WindowsDirectoryChannel> = preopenDirectory(
        realPath = currentWorkingDirectoryPath,
        baseDirectoryChannel = null,
    )

    val cwdChannel: WindowsDirectoryChannel? = currentWorkingDirectory.getOrNull()

    val opened: MutableMap<RealPath, WindowsDirectoryChannel> = mutableMapOf()
    val directories: Either<BatchDirectoryOpenerError, PreopenedDirectories> = either {
        for (directory in preopenedDirectories) {
            val realPath = directory.realPath

            if (opened.containsKey(realPath)) {
                continue
            }

            val fdChannel = preopenDirectory(realPath, cwdChannel)
                .mapLeft { BatchDirectoryOpenerError(directory, it) }
                .bind()

            opened[realPath] = fdChannel
        }

        PreopenedDirectories(currentWorkingDirectory, opened)
    }.onLeft {
        opened.values.closeSilent()
    }
    return directories
}

private fun preopenDirectory(
    realPath: RealPath,
    baseDirectoryChannel: WindowsDirectoryChannel?,
): Either<OpenError, WindowsDirectoryChannel> {
    val virtualPath = generatePreopenedDirectoryVirtualPath(realPath).mapLeft { InvalidArgument(it.message) }
        .getOrElse { return it.left() }

    return windowsNtOpenDirectory(
        path = realPath,
        rootHandle = baseDirectoryChannel?.handle,
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

internal class PreopenedDirectories(
    val currentWorkingDirectory: Either<OpenError, WindowsDirectoryChannel>,
    val preopenedDirectories: Map<String, WindowsDirectoryChannel>,
)

internal data class BatchDirectoryOpenerError(
    val directory: PreopenedDirectory,
    val error: OpenError,
)

internal fun Collection<WindowsDirectoryChannel>.closeSilent() = this.forEach { dsResource ->
    dsResource.handle.close().onLeft {
        // IGNORE
    }
}
