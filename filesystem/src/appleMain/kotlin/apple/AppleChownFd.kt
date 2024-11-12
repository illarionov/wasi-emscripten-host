/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.chown.ChownFd

internal class AppleChownFd(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<ChownFd, ChownError, Unit> {
    override fun invoke(input: ChownFd): Either<ChownError, Unit> {
        return fsState.executeWithResource(input.fd) { fdResource ->
            fdResource.chown(input.owner, input.group)
        }
    }
}
