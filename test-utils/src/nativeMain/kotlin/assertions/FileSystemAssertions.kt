/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.utils.assertions

import assertk.Assert
import assertk.assertions.support.appendName
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.S_IRGRP
import platform.posix.S_IROTH
import platform.posix.S_IRUSR
import platform.posix.S_IWGRP
import platform.posix.S_IWOTH
import platform.posix.S_IWUSR
import platform.posix.S_IXGRP
import platform.posix.S_IXOTH
import platform.posix.S_IXUSR

internal expect fun Path.getFileMode(): UInt

public fun Assert<Path>.fileMode(
    withSuidGidSticky: Boolean = false,
): Assert<Set<FileModeBit>> = transform(appendName("FileModeFull", separator = ".")) { path ->
    val absolutePath = SystemFileSystem.resolve(path)
    val fileMode = absolutePath.getFileMode()
    val mode = fileModeToPosixMode(fileMode)
    if (withSuidGidSticky) {
        mode
    } else {
        mode - setOf(FileModeBit.SUID, FileModeBit.SGID, FileModeBit.STICKY)
    }
}

public enum class FileModeBit(
    internal val posixMask: UInt,
) {
    SUID(0b100_000_000_000U),
    SGID(0b010_000_000_000U),
    STICKY(0b001_000_000_000U),
    USER_READ(S_IRUSR.toUInt()),
    USER_WRITE(S_IWUSR.toUInt()),
    USER_EXECUTE(S_IXUSR.toUInt()),
    GROUP_READ(S_IRGRP.toUInt()),
    GROUP_WRITE(S_IWGRP.toUInt()),
    GROUP_EXECUTE(S_IXGRP.toUInt()),
    OTHER_READ(S_IROTH.toUInt()),
    OTHER_WRITE(S_IWOTH.toUInt()),
    OTHER_EXECUTE(S_IXOTH.toUInt()),
}

internal fun fileModeToPosixMode(umode: UInt): Set<FileModeBit> = FileModeBit.entries.filter {
    umode and it.posixMask == it.posixMask
}.toSet()
