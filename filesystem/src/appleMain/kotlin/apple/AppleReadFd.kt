/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readwrite.ReadFd

internal class AppleReadFd(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<ReadFd, ReadError, ULong> {
    override fun invoke(input: ReadFd): Either<ReadError, ULong> = fsState.executeWithResource(input.fd) {
        it.read(input.iovecs, input.strategy)
    }
}
