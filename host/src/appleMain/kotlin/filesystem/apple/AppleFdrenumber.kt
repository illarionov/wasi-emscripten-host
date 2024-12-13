/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.error.FdrenumberError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.fdrenumber.Fdrenumber

internal class AppleFdrenumber(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<Fdrenumber, FdrenumberError, Unit> {
    override fun invoke(input: Fdrenumber): Either<FdrenumberError, Unit> {
        return fsState.renumber(input.fromFd, input.toFd)
    }
}
