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
import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSource

@Suppress("LoopWithTooManyJumpStatements", "SwallowedException")
internal fun RawSource.transferTo(
    iovecs: List<FileSystemByteBuffer>,
): Either<ReadError, ULong> = either {
    val buffer = Buffer()
    var totalBytesMoved = 0UL
    for (iovec in iovecs) {
        if (iovec.length == 0) {
            continue
        }

        check(buffer.size == 0L)
        val bytesRead = try {
            this@transferTo.readAtMostTo(buffer, iovec.length.toLong()).toInt()
        } catch (iae: IllegalArgumentException) {
            raise(InvalidArgument("Incorrect iovec length ${iovec.length}"))
        } catch (ise: IllegalStateException) {
            raise(BadFileDescriptor("Source closed"))
        } catch (ioe: IOException) {
            raise(IoError("I/O error: ${ioe.message}"))
        }

        if (bytesRead == -1) {
            // Source is exhausted
            break
        }

        val bytesMoved = buffer.readAtMostTo(iovec.array, iovec.offset, iovec.offset + bytesRead)
        check(bytesMoved == bytesRead)

        totalBytesMoved += bytesRead.toULong()

        if (bytesRead < iovec.length) {
            break
        }
    }
    totalBytesMoved
}
