/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.internal.delegatefs.DelegateOperationsFileSystem
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StandardInputOutput

internal class AppleFileSystemImpl(
    interceptors: List<FileSystemInterceptor>,
    stdio: StandardInputOutput,
    isRootAccessAllowed: Boolean,
    currentWorkingDirectory: String?,
    preopenedDirectories: List<PreopenedDirectory>,
) : FileSystem {
    private val fsState = AppleFileSystemState.create(
        stdio = stdio,
        isRootAccessAllowed = isRootAccessAllowed,
        currentWorkingDirectory = currentWorkingDirectory ?: "",
        preopenedDirectories = preopenedDirectories,
    )
    private val operations: Map<FileSystemOperation<*, *, *>, FileSystemOperationHandler<*, *, *>> = mapOf(
        Open to AppleOpen(fsState),
    )
    private val fsAdapter = DelegateOperationsFileSystem(operations, interceptors)

    override fun close() {
        fsState.close()
    }

    override fun <I : Any, E : FileSystemOperationError, R : Any> execute(
        operation: FileSystemOperation<I, E, R>,
        input: I,
    ): Either<E, R> = fsAdapter.execute(operation, input)

    override fun isOperationSupported(operation: FileSystemOperation<*, *, *>): Boolean {
        return fsAdapter.isOperationSupported(operation)
    }
}
