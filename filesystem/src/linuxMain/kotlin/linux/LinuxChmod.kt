/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.linuxChmod
import at.released.weh.filesystem.op.chmod.Chmod
import at.released.weh.filesystem.path.PosixPathConverter.convertToRealPath

internal class LinuxChmod(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<Chmod, ChmodError, Unit> {
    override fun invoke(input: Chmod): Either<ChmodError, Unit> =
        convertToRealPath(input.path)
            .mapLeft { InvalidArgument(it.message) }
            .flatMap { inputRealPath ->
                fsState.executeWithBaseDirectoryResource(input.baseDirectory) {
                    linuxChmod(it, inputRealPath, input.mode, input.followSymlinks)
                }
            }
}
