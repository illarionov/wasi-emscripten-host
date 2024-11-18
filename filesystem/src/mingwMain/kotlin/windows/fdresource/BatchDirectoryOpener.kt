/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.fdresource

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.preopened.RealPath
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.nativefunc.ntCreateFileEx
import at.released.weh.filesystem.windows.nativefunc.windowsCloseHandle
import platform.windows.FILE_ATTRIBUTE_DIRECTORY
import platform.windows.FILE_DIRECTORY_FILE
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_OPEN
import platform.windows.FILE_OPEN_FOR_BACKUP_INTENT

internal fun preopenDirectories(
    currentWorkingDirectoryPath: RealPath = ".",
    preopenedDirectories: List<PreopenedDirectory> = listOf(),
): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
    val currentWorkingDirectory: Either<OpenError, WindowsDirectoryChannel> = preopenDirectory(
        path = currentWorkingDirectoryPath,
        baseDirectoryChannel = null,
    )

    val cwdChannel: WindowsDirectoryChannel? = currentWorkingDirectory.getOrNull()

    val opened: MutableMap<RealPath, WindowsDirectoryChannel> = mutableMapOf()
    val directories: Either<BatchDirectoryOpenerError, PreopenedDirectories> = either {
        for (directory in preopenedDirectories) {
            val realpath = directory.realPath

            if (opened.containsKey(realpath)) {
                continue
            }

            val fdChannel = preopenDirectory(realpath, cwdChannel)
                .mapLeft { BatchDirectoryOpenerError(directory, it) }
                .bind()

            opened[realpath] = fdChannel
        }

        PreopenedDirectories(currentWorkingDirectory, opened)
    }.onLeft {
        opened.values.closeSilent()
    }
    return directories
}

private fun preopenDirectory(
    path: RealPath,
    baseDirectoryChannel: WindowsDirectoryChannel?,
): Either<OpenError, WindowsDirectoryChannel> {
    return ntCreateFileEx(
        rootHandle = baseDirectoryChannel?.handle,
        path = path,
        desiredAccess = FILE_LIST_DIRECTORY,
        fileAttributes = FILE_ATTRIBUTE_DIRECTORY,
        createDisposition = FILE_OPEN,
        createOptions = FILE_DIRECTORY_FILE or FILE_OPEN_FOR_BACKUP_INTENT,
        caseSensitive = true,
        followSymlinks = true,
    ).map { newHandle ->
        WindowsDirectoryChannel(
            handle = newHandle,
            isPreopened = true,
            rights = FdRightsBlock.DIRECTORY_BASE_RIGHTS_BLOCK,
            virtualPath = path,
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
    windowsCloseHandle(dsResource.handle).onLeft {
        // IGNORE
    }
}
