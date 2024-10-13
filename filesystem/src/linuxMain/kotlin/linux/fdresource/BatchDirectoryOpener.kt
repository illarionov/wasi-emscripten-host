/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.fdresource

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.linux.native.linuxOpen
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeDirectoryFd.Companion.CURRENT_WORKING_DIRECTORY
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.preopened.RealPath

internal fun preopenDirectories(
    currentWorkingDirectoryPath: RealPath = "",
    preopenedDirectories: List<PreopenedDirectory> = listOf(),
): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
    val currentWorkingDirectory: Either<OpenError, LinuxDirectoryFdResource> = preopenDirectory(
        path = currentWorkingDirectoryPath,
        baseDirectoryFd = CURRENT_WORKING_DIRECTORY,
    )

    val cwdFd = currentWorkingDirectory.fold(
        ifLeft = { NativeDirectoryFd(-1) },
        ifRight = LinuxDirectoryFdResource::nativeFd,
    )
    val opened: MutableMap<RealPath, LinuxDirectoryFdResource> = mutableMapOf()
    val directories: Either<BatchDirectoryOpenerError, PreopenedDirectories> = either {
        for (directory in preopenedDirectories) {
            val realpath = directory.realPath

            if (opened.containsKey(realpath)) {
                continue
            }

            val fdResource = preopenDirectory(realpath, cwdFd)
                .mapLeft { BatchDirectoryOpenerError(directory, it) }
                .bind()

            opened[realpath] = fdResource
        }

        PreopenedDirectories(currentWorkingDirectory, opened)
    }.onLeft {
        opened.values.closeSilent()
    }
    return directories
}

private fun preopenDirectory(
    path: RealPath,
    baseDirectoryFd: NativeDirectoryFd,
): Either<OpenError, LinuxDirectoryFdResource> {
    return linuxOpen(
        baseDirectoryFd = baseDirectoryFd,
        path = path,
        flags = OpenFileFlag.O_PATH,
        fdFlags = 0,
        mode = 0,
    ).map { nativeFd: Int ->
        LinuxDirectoryFdResource(NativeDirectoryFd(nativeFd), true, virtualPath = path)
    }
}

internal class PreopenedDirectories(
    val currentWorkingDirectory: Either<OpenError, LinuxDirectoryFdResource>,
    val preopenedDirectories: Map<String, LinuxDirectoryFdResource>,
)

internal data class BatchDirectoryOpenerError(
    val directory: PreopenedDirectory,
    val error: OpenError,
)

internal fun Collection<LinuxDirectoryFdResource>.closeSilent() = this.forEach { dsResource ->
    dsResource.close().onLeft {
        // IGNORE
    }
}
