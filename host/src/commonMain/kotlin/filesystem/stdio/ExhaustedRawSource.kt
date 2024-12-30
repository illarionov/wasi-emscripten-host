/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.stdio

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import kotlinx.io.Buffer
import kotlin.concurrent.Volatile

internal class ExhaustedRawSource(
    @Volatile private var isClosed: Boolean = false,
) : StdioSource {
    override fun close() {
        isClosed = true
    }

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        checkSourceNotClosed()
        require(byteCount >= 0)
        return -1
    }

    override fun pollNonblocking(): Either<NonblockingPollError, StdioPollEvent> {
        return StdioPollEvent(
            errno = BADF,
            bytesAvailable = 0,
            isHangup = true,
        ).right()
    }

    private fun checkSourceNotClosed(): Unit = check(!isClosed) { "Source is closed" }
}
