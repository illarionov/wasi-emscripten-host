/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.error.SetFdFlagsError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.setfdflags.SetFdFlags

internal class AppleSetFdFlags(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<SetFdFlags, SetFdFlagsError, Unit> {
    override fun invoke(input: SetFdFlags): Either<SetFdFlagsError, Unit> = fsState.executeWithResource(input.fd) {
        it.setFdFlags(input.flags)
    }
}