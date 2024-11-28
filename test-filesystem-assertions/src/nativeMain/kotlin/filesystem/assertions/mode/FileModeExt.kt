/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.filesystem.assertions.mode

import platform.posix.S_IRGRP
import platform.posix.S_IROTH
import platform.posix.S_IRUSR
import platform.posix.S_IWGRP
import platform.posix.S_IWOTH
import platform.posix.S_IWUSR
import platform.posix.S_IXGRP
import platform.posix.S_IXOTH
import platform.posix.S_IXUSR

internal fun fileModeToPosixFileModeBits(umode: UInt): Set<PosixFileModeBit> = PosixFileModeBit.entries.filter {
    umode and it.posixMask == it.posixMask
}.toSet()

private val PosixFileModeBit.posixMask: UInt get() = when (this) {
    PosixFileModeBit.SUID -> 0b100_000_000_000U
    PosixFileModeBit.SGID -> 0b010_000_000_000U
    PosixFileModeBit.STICKY -> 0b001_000_000_000U
    PosixFileModeBit.USER_READ -> S_IRUSR.toUInt()
    PosixFileModeBit.USER_WRITE -> S_IWUSR.toUInt()
    PosixFileModeBit.USER_EXECUTE -> S_IXUSR.toUInt()
    PosixFileModeBit.GROUP_READ -> S_IRGRP.toUInt()
    PosixFileModeBit.GROUP_WRITE -> S_IWGRP.toUInt()
    PosixFileModeBit.GROUP_EXECUTE -> S_IXGRP.toUInt()
    PosixFileModeBit.OTHER_READ -> S_IROTH.toUInt()
    PosixFileModeBit.OTHER_WRITE -> S_IWOTH.toUInt()
    PosixFileModeBit.OTHER_EXECUTE -> S_IXOTH.toUInt()
}
