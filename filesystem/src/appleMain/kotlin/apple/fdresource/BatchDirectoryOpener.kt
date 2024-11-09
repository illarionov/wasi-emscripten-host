/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.fdresource

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.apple.nativefunc.appleOpenRaw
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeDirectoryFd.Companion.CURRENT_WORKING_DIRECTORY
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.preopened.RealPath

// TODO: merge with LinuxBatchDirectoryPreopener
internal fun preopenDirectories(
    currentWorkingDirectoryPath: RealPath = "",
    preopenedDirectories: List<PreopenedDirectory> = listOf(),
): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
    val currentWorkingDirectory: Either<OpenError, AppleDirectoryFdResource> = preopenDirectory(
        path = currentWorkingDirectoryPath,
        baseDirectoryFd = CURRENT_WORKING_DIRECTORY,
    )

    val cwdFd = currentWorkingDirectory.fold(
        ifLeft = { NativeDirectoryFd(-1) },
        ifRight = AppleDirectoryFdResource::nativeFd,
    )
    val opened: MutableMap<RealPath, AppleDirectoryFdResource> = mutableMapOf()
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
): Either<OpenError, AppleDirectoryFdResource> {
    return appleOpenRaw(
        baseDirectoryFd = baseDirectoryFd,
        path = path,
        flags = OpenFileFlag.O_PATH,
        fdFlags = 0,
        mode = 0,
    ).map { nativeFd: Int ->
        AppleDirectoryFdResource(
            nativeFd = NativeDirectoryFd(nativeFd),
            isPreopened = true,
            virtualPath = path,
            rights = FdRightsBlock.DIRECTORY_BASE_RIGHTS_BLOCK,
        )
    }
}

internal class PreopenedDirectories(
    val currentWorkingDirectory: Either<OpenError, AppleDirectoryFdResource>,
    val preopenedDirectories: Map<RealPath, AppleDirectoryFdResource>,
)

internal data class BatchDirectoryOpenerError(
    val directory: PreopenedDirectory,
    val error: OpenError,
)

internal fun Collection<AppleDirectoryFdResource>.closeSilent() = this.forEach { dsResource ->
    dsResource.close().onLeft {
        // IGNORE
    }
}
