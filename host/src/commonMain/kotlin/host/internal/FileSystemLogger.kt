/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.internal

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents.OperationEnd
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.OperationLoggingLevel.BASIC

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
