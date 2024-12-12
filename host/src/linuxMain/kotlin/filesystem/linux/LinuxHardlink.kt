/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.HardlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.linuxHardlink
import at.released.weh.filesystem.op.hardlink.Hardlink

internal class LinuxHardlink(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<Hardlink, HardlinkError, Unit> {
    override fun invoke(input: Hardlink): Either<HardlinkError, Unit> {
        return fsState.executeWithPath(input.oldPath, input.oldBaseDirectory) { oldRealPath, oldRealBaseDirectory ->
            fsState.executeWithPath(input.newPath, input.newBaseDirectory) { newRealPath, newRealBaseDirectory ->
                linuxHardlink(
                    oldRealBaseDirectory,
                    oldRealPath,
                    newRealBaseDirectory,
                    newRealPath,
                    input.followSymlinks,
                )
            }
        }
    }
}
