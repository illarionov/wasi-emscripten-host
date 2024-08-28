/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.op.stat.StructStat
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.errno
import platform.posix.fstat
import platform.posix.stat

internal actual fun platformFstatFd(fd: Int): Either<Int, StructStat> = memScoped {
    val statBuf: stat = alloc()
    val exitCode = fstat(
        fd,
        statBuf.ptr,
    )
    return if (exitCode == 0) {
        statBuf.toStructStat().right()
    } else {
        errno.left()
    }
}
