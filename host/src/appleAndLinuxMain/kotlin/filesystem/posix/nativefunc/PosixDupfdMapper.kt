/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.nativefunc

import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.Mfile
import at.released.weh.filesystem.error.ReadDirError
import kotlinx.cinterop.toKStringFromUtf8
import platform.posix.EBADF
import platform.posix.EBUSY
import platform.posix.EINTR
import platform.posix.EMFILE
import platform.posix.strerror

internal object PosixDupfdMapper {
    fun dupErrorToReadDirError(errno: Int): ReadDirError = when (errno) {
        EBADF -> BadFileDescriptor("Bad file descriptor")
        EBUSY -> IoError("EBUSY")
        EINTR -> IoError("Interrupted by signal")
        EMFILE -> Mfile("Too many open files")
        else -> IoError("Other error `$this` (${strerror(errno)?.toKStringFromUtf8()})")
    }
}
