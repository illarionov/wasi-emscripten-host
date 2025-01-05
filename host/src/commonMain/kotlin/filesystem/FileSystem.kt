/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem

import arrow.core.Either
import at.released.weh.host.FileSystemSimpleConfigBlock
import at.released.weh.filesystem.dsl.FileSystemCommonConfig
import at.released.weh.filesystem.dsl.FileSystemConfigBlock
import at.released.weh.filesystem.dsl.FileSystemEngineConfig
import at.released.weh.filesystem.dsl.StandardInputOutputConfigBlock
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.preopened.PreopenedDirectory

public fun <E : FileSystemEngineConfig> FileSystem(
    engine: FileSystemEngine<E>,
    block: FileSystemConfigBlock<E>.() -> Unit = {},
): FileSystem {
    val config = FileSystemConfigBlock<E>().apply(block)
    val stdioConfig = StandardInputOutputConfigBlock().apply(config.stdioConfig)
    val commonConfig = object : FileSystemCommonConfig {
        override val interceptors: List<FileSystemInterceptor> = config.interceptors
        override val stdioConfig: StandardInputOutputConfigBlock = stdioConfig
        override val isRootAccessAllowed: Boolean = config.isRootAccessAllowed
        override val currentWorkingDirectory: String? = config.currentWorkingDirectory
        override val preopenedDirectories: List<PreopenedDirectory> = config.preopenedDirectories
    }
    return engine.create(
        commonConfig = commonConfig,
        engineConfig = config.engineConfig,
    )
}

public interface FileSystem : AutoCloseable {
    public fun <I : Any, E : FileSystemOperationError, R : Any> execute(
        operation: FileSystemOperation<I, E, R>,
        input: I,
    ): Either<E, R>

    public fun isOperationSupported(operation: FileSystemOperation<*, *, *>): Boolean
}
