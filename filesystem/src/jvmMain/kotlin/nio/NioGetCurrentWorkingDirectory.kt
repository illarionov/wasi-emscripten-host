/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import at.released.weh.filesystem.error.GetCurrentWorkingDirectoryError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.CurrentDirectoryProvider
import at.released.weh.filesystem.op.cwd.GetCurrentWorkingDirectory
import java.nio.file.Path

internal class NioGetCurrentWorkingDirectory(
    private val currentDirectoryProvider: CurrentDirectoryProvider,
) : FileSystemOperationHandler<GetCurrentWorkingDirectory, GetCurrentWorkingDirectoryError, String> {
    override fun invoke(input: GetCurrentWorkingDirectory): Either<GetCurrentWorkingDirectoryError, String> {
        return currentDirectoryProvider.getCurrentWorkingDirectory().map(Path::toString)
    }
}
