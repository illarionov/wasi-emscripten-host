/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.linuxChmod
import at.released.weh.filesystem.op.chmod.Chmod

internal class LinuxChmod(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<Chmod, ChmodError, Unit> {
    override fun invoke(input: Chmod): Either<ChmodError, Unit> =
        fsState.executeWithPath(input.path, input.baseDirectory) { realPath, realBaseDirectory ->
            linuxChmod(realBaseDirectory, realPath, input.mode, input.followSymlinks)
        }
}
