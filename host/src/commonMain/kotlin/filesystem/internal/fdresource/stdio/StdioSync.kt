/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.fdresource.stdio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.io.IOException
import kotlinx.io.RawSink

internal fun RawSink.flushNoThrow(): Either<StdioReadWriteError, Unit> = try {
    this.flush()
    Unit.right()
} catch (ise: IllegalStateException) {
    StdioReadWriteError.Closed("Sink closed: ${ise.message}").left()
} catch (ioe: IOException) {
    StdioReadWriteError.IoError("Flush() failed: ${ioe.message}").left()
}
