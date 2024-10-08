/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.ext

import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.Nxio
import at.released.weh.filesystem.error.Overflow
import at.released.weh.filesystem.error.Pipe
import at.released.weh.filesystem.error.SeekError
import platform.posix.EBADF
import platform.posix.EINVAL
import platform.posix.ENXIO
import platform.posix.EOVERFLOW
import platform.posix.ESPIPE

internal fun Int.errnoToSeekError(): SeekError = when (this) {
    EBADF -> BadFileDescriptor("Bad file descriptor")
    EINVAL -> InvalidArgument("Whence is not valid.")
    ENXIO -> Nxio("Invalid offset.")
    EOVERFLOW -> Overflow("Resulting offset is out of range.")
    ESPIPE -> Pipe("fd is not a file")
    else -> InvalidArgument("Other error. Errno: $this")
}
