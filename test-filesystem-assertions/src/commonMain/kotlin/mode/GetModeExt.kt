/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.filesystem.assertions.mode

import assertk.Assert
import assertk.assertions.isEqualTo
import assertk.assertions.support.appendName
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

public expect val Path.isPosixFileModeSupported: Boolean

internal expect fun Path.getFileMode(): Set<PosixFileModeBit>

public fun Assert<Path>.posixFileMode(
    withSuidGidSticky: Boolean = false,
): Assert<Set<PosixFileModeBit>> = transform(appendName("FileMode", separator = ".")) { path ->
    val fileMode = SystemFileSystem.resolve(path).getFileMode()
    if (withSuidGidSticky) {
        fileMode
    } else {
        fileMode - setOf(PosixFileModeBit.SUID, PosixFileModeBit.SGID, PosixFileModeBit.STICKY)
    }
}

public fun Assert<Path>.posixFileModeIfSupportedIsEqualTo(
    vararg expectedBits: PosixFileModeBit,
    withSuidGidSticky: Boolean = false,
): Unit = given { path ->
    if (!path.isPosixFileModeSupported) {
        return
    }
    assertThat(path).posixFileMode(withSuidGidSticky).isEqualTo(expectedBits.toSet())
}
