/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.nativefunc

import arrow.core.Either
import arrow.core.right
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.posix.FIONREAD
import platform.posix.ioctlsocket

internal actual fun nativeFdBytesAvailable(fd: NativeFileFd): Either<Int, Int> = memScoped {
    val intptr: UIntVar = alloc()

    // XXX not working, find better solution
    val result = ioctlsocket(fd.fd.toULong(), FIONREAD, intptr.ptr)
    return if (result >= 0) {
        intptr.value.toInt().right()
    } else {
        0.right()
    }
}
