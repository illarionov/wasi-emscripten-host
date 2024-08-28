/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem

import arrow.core.Either
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.op.FileSystemOperation

@InternalWasiEmscriptenHostApi
public object NotImplementedFileSystem : FileSystem {
    override fun <I : Any, E : FileSystemOperationError, R : Any> execute(
        operation: FileSystemOperation<I, E, R>,
        input: I,
    ): Either<E, R> {
        error("Not implemented")
    }

    override fun isOperationSupported(operation: FileSystemOperation<*, *, *>): Boolean {
        error("Not implemented")
    }

    override fun close() {
        error("Not implemented")
    }
}
