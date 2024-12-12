/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple.fdresource

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import at.released.weh.filesystem.apple.nativefunc.appleOpenRaw
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.posix.PosixPathConverter.toVirtualPath
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.toCommonError
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeDirectoryFd.Companion.CURRENT_WORKING_DIRECTORY
import at.released.weh.filesystem.preopened.PreopenedDirectory

// TODO: merge with LinuxBatchDirectoryPreopener
internal fun preopenDirectories(
    currentWorkingDirectoryPath: String?,
    preopenedDirectories: List<PreopenedDirectory> = listOf(),
): Either<BatchDirectoryOpenerError, PreopenedDirectories> {
    val currentWorkingDirectory: Either<OpenError, AppleDirectoryFdResource> =
        if (currentWorkingDirectoryPath != null) {
            PosixRealPath.create(currentWorkingDirectoryPath)
                .mapLeft { it.toCommonError() }
                .flatMap { cwdRealPath ->
                    preopenDirectory(
                        path = cwdRealPath,
                        baseDirectoryFd = CURRENT_WORKING_DIRECTORY,
                    )
                }
        } else {
            NoEntry("Current working directory not set").left()
        }

    val cwdFd = if (currentWorkingDirectoryPath != null) {
        currentWorkingDirectory.fold(
            ifLeft = { NativeDirectoryFd(-1) },
            ifRight = AppleDirectoryFdResource::nativeFd,
        )
    } else {
        CURRENT_WORKING_DIRECTORY
    }

    val opened: MutableList<AppleDirectoryFdResource> = mutableListOf()
    val directories: Either<BatchDirectoryOpenerError, PreopenedDirectories> = either {
        preopenedDirectories.toSet().mapTo(opened) { directory ->
            PosixRealPath.create(directory.realPath)
                .mapLeft<ResolveRelativePathErrors>(PathError::toCommonError)
                .flatMap { realPath -> preopenDirectory(realPath, cwdFd) }
                .mapLeft { BatchDirectoryOpenerError(directory, it) }
                .bind()
        }
        PreopenedDirectories(currentWorkingDirectory, opened)
    }.onLeft {
        opened.closeSilent()
    }
    return directories
}

private fun preopenDirectory(
    path: PosixRealPath,
    baseDirectoryFd: NativeDirectoryFd,
): Either<OpenError, AppleDirectoryFdResource> {
    val virtualPath = toVirtualPath(path).getOrElse { return it.toCommonError().left() }

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
            virtualPath = virtualPath,
            rights = FdRightsBlock.DIRECTORY_BASE_RIGHTS_BLOCK,
        )
    }
}

internal data class PreopenedDirectories(
    val currentWorkingDirectory: Either<OpenError, AppleDirectoryFdResource>,
    val preopenedDirectories: List<AppleDirectoryFdResource>,
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
