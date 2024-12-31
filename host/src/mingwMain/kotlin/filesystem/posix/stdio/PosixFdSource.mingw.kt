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
import platform.posix.read

internal actual fun readNative(
    fd: NativeFileFd,
    buf: CValuesRef<*>,
    count: Int,
): Either<Int, Int> {
    val bytes = read(fd.fd, buf, count.toUInt())
    return if (bytes > 0) {
        bytes.right()
    } else {
        errno.left()
    }
}
