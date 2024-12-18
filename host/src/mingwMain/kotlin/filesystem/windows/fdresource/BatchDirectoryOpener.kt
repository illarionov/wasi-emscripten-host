/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.fdresource

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter.convertToVirtualPath
import at.released.weh.filesystem.path.real.windows.WindowsPathType
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import at.released.weh.filesystem.path.toCommonError
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.windows.fdresource.WindowsDirectoryFdResource.WindowsDirectoryChannel
import at.released.weh.filesystem.windows.path.NtPath
import at.released.weh.filesystem.windows.win32api.close
import at.released.weh.filesystem.windows.win32api.createfile.windowsNtOpenDirectory
import at.released.weh.filesystem.windows.win32api.windowsDosPathNameToNtPathName
import platform.windows.FILE_LIST_DIRECTORY
import platform.windows.FILE_READ_ATTRIBUTES
import platform.windows.FILE_TRAVERSE
import platform.windows.FILE_WRITE_ATTRIBUTES
import platform.windows.HANDLE

internal fun preopenDirectories(
    currentWorkingDirectoryPath: String?,
    preopenedDirectories: List<PreopenedDirectory> = listOf(),
): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
    val currentWorkingDirectory: Either<OpenError, WindowsDirectoryChannel> = if (currentWorkingDirectoryPath != null) {
        WindowsRealPath.create(currentWorkingDirectoryPath)
            .mapLeft { it.toCommonError() }
            .flatMap { cwdRealPath -> preopenDirectory(realPath = cwdRealPath, cwdHandle = null) }
    } else {
        NoEntry("Current working directory not set").left()
    }

    val cwdHandle = currentWorkingDirectory.getOrNull()?.handle

    val opened: MutableMap<String, WindowsDirectoryChannel> = mutableMapOf()
    val directories: Either<BatchDirectoryOpenerError, PreopenedDirectories> = either {
        for (directory in preopenedDirectories) {
            val realPathString = directory.realPath
            opened.getOrPut(realPathString) {
                WindowsRealPath.create(realPathString)
                    .mapLeft<ResolveRelativePathErrors>(PathError::toCommonError)
                    .flatMap { realPath -> preopenDirectory(realPath, cwdHandle) }
                    .mapLeft { BatchDirectoryOpenerError(directory, it) }
                    .bind()
            }
        }
        PreopenedDirectories(currentWorkingDirectory, opened)
    }.onLeft {
        opened.values.closeSilent()
    }
    return directories
}

private fun preopenDirectory(
    realPath: WindowsRealPath,
    cwdHandle: HANDLE?,
): Either<OpenError, WindowsDirectoryChannel> {
    val virtualPath = convertToVirtualPath(realPath)
        .getOrElse { return InvalidArgument(it.message).left() }

    val ntPath = when {
        realPath.type == WindowsPathType.RELATIVE && cwdHandle != null -> {
            // Resolve relative to cwdHandle
            WindowsNtRelativePath.createFromRelativeWindowsPath(realPath)
                .map { NtPath.Relative(cwdHandle, it) }
        }

        else -> windowsDosPathNameToNtPathName(realPath.kString).map { NtPath.Absolute(it) }
    }.getOrElse { return it.toCommonError().left() }

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
