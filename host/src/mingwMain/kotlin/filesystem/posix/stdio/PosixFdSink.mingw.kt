/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.stdio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.toLong
import platform.posix.EBADF
import platform.posix._get_osfhandle
import platform.posix.errno
import platform.posix.intptr_t
import platform.posix.write
import platform.windows.FlushFileBuffers
import platform.windows.INVALID_HANDLE_VALUE

internal actual fun syncNative(fd: Int): Either<Int, Unit> {
    val handle: intptr_t = _get_osfhandle(fd)
    if (handle == INVALID_HANDLE_VALUE.toLong()) {
        return EBADF.left()
    }
    val result = FlushFileBuffers(handle.toCPointer())
    return if (result == 0) {
        EBADF.left()
    } else {
        Unit.right()
    }
}

internal actual fun writeNative(
    fd: Int,
    buf: CValuesRef<*>,
    bytes: Int,
): Either<Int, Int> {
    val bytesWritten = write(fd, buf, bytes.toUInt())
    return if (bytes >= 0) {
        bytesWritten.right()
    } else {
        errno.left()
    }
}
