/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.readdir

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.BadFileDescriptor
import kotlinx.cinterop.CPointer
import platform.posix.DIR
import platform.posix.EBADF
import platform.posix.dirent
import platform.posix.errno
import platform.posix.telldir

internal actual fun getCookie(
    dir: CPointer<DIR>,
    dirent: dirent,
): Either<BadFileDescriptor, Long> {
    val resultCode = telldir(dir)
    return if (resultCode != -1L) {
        resultCode.right()
    } else {
        when (val error = errno) {
            EBADF -> BadFileDescriptor("Invalid directory").left()
            else -> BadFileDescriptor("Other error (errno $error)").left()
        }
    }
}
