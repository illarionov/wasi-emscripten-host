/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.internal

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemEngine
import at.released.weh.filesystem.dsl.FileSystemConfigBlock
import at.released.weh.filesystem.dsl.FileSystemEngineConfig
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents.OperationEnd
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.OperationLoggingLevel.BASIC
import at.released.weh.filesystem.stdio.SinkProvider
import at.released.weh.filesystem.stdio.SourceProvider

internal operator fun LoggingFileSystemInterceptor.Companion.invoke(
    logger: Logger,
): LoggingFileSystemInterceptor {
    return LoggingFileSystemInterceptor(
        logger = { logger.v(null, it) },
        logEvents = LoggingEvents(
            end = OperationEnd(
                inputs = BASIC,
                outputs = BASIC,
                trackDuration = false,
            ),
        ),
    )
}

@Suppress("FunctionName")
internal fun <E : FileSystemEngineConfig> DefaultFileSystem(
    engine: FileSystemEngine<E>,
    stdinProvider: SourceProvider?,
    stdoutProvider: SinkProvider?,
    stderrProvider: SinkProvider?,
    rootLogger: Logger,
    block: FileSystemConfigBlock<E>.() -> Unit = {},
): FileSystem = FileSystem(engine) {
    addInterceptor(LoggingFileSystemInterceptor(rootLogger))
    stdio {
        this.stdinProvider = stdinProvider
        this.stdoutProvider = stdoutProvider
        this.stderrProvider = stderrProvider
    }
    block()
}
