/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.stdio

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.CValuesRef
import platform.posix.errno
import platform.posix.fsync
import platform.posix.write

internal actual fun syncNative(fd: NativeFileFd): Either<Int, Unit> {
    val result = fsync(fd.fd)
    return if (result == 0) {
        Unit.right()
    } else {
        errno.left()
    }
}

internal actual fun writeNative(
    fd: NativeFileFd,
    buf: CValuesRef<*>,
    bytes: Int,
): Either<Int, Int> {
    val bytesWritten = write(fd.fd, buf, bytes.toULong())
    return if (bytes >= 0) {
        bytesWritten.toInt().right()
    } else {
        errno.left()
    }
}
