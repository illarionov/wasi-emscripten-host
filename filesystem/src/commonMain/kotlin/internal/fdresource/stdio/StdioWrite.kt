/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.fdresource.stdio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSink

internal fun RawSink.transferFrom(
    cIovecs: List<FileSystemByteBuffer>,
): Either<WriteError, ULong> = either {
    val buffer = Buffer()
    var totalBytesMoved = 0UL
    for (ciovec in cIovecs) {
        if (ciovec.length == 0) {
            continue
        }
        check(buffer.size == 0L)
        @Suppress("SwallowedException")
        try {
            buffer.write(ciovec.array, ciovec.offset, ciovec.offset + ciovec.length)
            this@transferFrom.write(buffer, ciovec.length.toLong())
            totalBytesMoved += ciovec.length.toULong()
        } catch (iae: IllegalArgumentException) {
            raise(InvalidArgument("Incorrect iovec length ${ciovec.length}"))
        } catch (ise: IllegalStateException) {
            raise(BadFileDescriptor("Sink closed"))
        } catch (ioe: IOException) {
            raise(IoError("I/O error: ${ioe.message}"))
        }
    }
    totalBytesMoved
}
