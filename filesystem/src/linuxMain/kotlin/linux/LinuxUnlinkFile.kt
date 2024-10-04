/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.linuxUnlinkFile
import at.released.weh.filesystem.op.unlink.UnlinkFile

internal class LinuxUnlinkFile(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<UnlinkFile, UnlinkError, Unit> {
    override fun invoke(input: UnlinkFile): Either<UnlinkError, Unit> =
        fsState.executeWithBaseDirectoryResource(input.baseDirectory) {
            linuxUnlinkFile(it, input.path)
        }
}
