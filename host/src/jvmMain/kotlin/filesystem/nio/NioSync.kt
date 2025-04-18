/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import at.released.weh.filesystem.error.SyncError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.sync.SyncFd

internal class NioSync(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<SyncFd, SyncError, Unit> {
    override fun invoke(input: SyncFd): Either<SyncError, Unit> = fsState.executeWithResource(input.fd) {
        it.sync(input.syncMetadata)
    }
}
