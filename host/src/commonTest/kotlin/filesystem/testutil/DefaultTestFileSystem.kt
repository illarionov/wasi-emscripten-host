/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.testutil

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemEngine
import at.released.weh.filesystem.dsl.FileSystemConfigBlock
import at.released.weh.filesystem.dsl.FileSystemEngineConfig
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents.OperationEnd
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.OperationLoggingLevel.BASIC
import at.released.weh.test.logger.TestLogger

internal expect fun <E : FileSystemEngineConfig> getDefaultTestEngine(): FileSystemEngine<E>

internal fun <E : FileSystemEngineConfig> DefaultTestFileSystem(
    engine: FileSystemEngine<E> = getDefaultTestEngine(),
    rootLogger: Logger = TestLogger(),
    block: FileSystemConfigBlock<E>.() -> Unit = {},
): FileSystem {
    val loggingInterceptor = LoggingFileSystemInterceptor(
        logger = { rootLogger.v(null, it) },
        logEvents = LoggingEvents(
            end = OperationEnd(
                inputs = BASIC,
                outputs = BASIC,
                trackDuration = false,
            ),
        ),
    )
    return FileSystem(engine) {
        addInterceptor(loggingInterceptor)
        block()
    }
}
