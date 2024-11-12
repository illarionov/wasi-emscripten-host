/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleHardlink
import at.released.weh.filesystem.error.HardlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.hardlink.Hardlink

internal class AppleHardlink(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<Hardlink, HardlinkError, Unit> {
    override fun invoke(input: Hardlink): Either<HardlinkError, Unit> {
        return fsState.executeWithBaseDirectoryResource(input.newBaseDirectory) { newDirectoryFd ->
            fsState.executeWithBaseDirectoryResource(input.oldBaseDirectory) { oldDirectoryFd ->
                appleHardlink(oldDirectoryFd, input.oldPath, newDirectoryFd, input.newPath, input.followSymlinks)
            }
        }
    }
}
