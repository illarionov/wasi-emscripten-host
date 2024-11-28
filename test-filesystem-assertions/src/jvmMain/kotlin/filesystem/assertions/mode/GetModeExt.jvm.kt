/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.test.filesystem.assertions.mode

import kotlinx.io.files.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE
import java.nio.file.attribute.PosixFilePermission.GROUP_READ
import java.nio.file.attribute.PosixFilePermission.GROUP_WRITE
import java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OTHERS_READ
import java.nio.file.attribute.PosixFilePermission.OTHERS_WRITE
import java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import kotlin.io.path.fileAttributesView
import java.nio.file.Path as NioPath

public actual val Path.isPosixFileModeSupported: Boolean
    get() = try {
        NioPath.of(this.toString()).fileAttributesView<PosixFileAttributeView>()
        true
    } catch (_: UnsupportedOperationException) {
        false
    }

internal actual fun Path.getFileMode(): Set<PosixFileModeBit> {
    val mode = NioPath.of(this.toString()).fileAttributesView<PosixFileAttributeView>().readAttributes().permissions()
    return mode.toPosixFileModeBits()
}

private fun Set<PosixFilePermission>.toPosixFileModeBits(): Set<PosixFileModeBit> {
    return mapTo(mutableSetOf(), PosixFilePermission::fileModeBit)
}

private val PosixFilePermission.fileModeBit: PosixFileModeBit
    get() = when (this) {
        OWNER_READ -> PosixFileModeBit.USER_READ
        OWNER_WRITE -> PosixFileModeBit.USER_WRITE
        OWNER_EXECUTE -> PosixFileModeBit.USER_EXECUTE
        GROUP_READ -> PosixFileModeBit.GROUP_READ
        GROUP_WRITE -> PosixFileModeBit.GROUP_WRITE
        GROUP_EXECUTE -> PosixFileModeBit.GROUP_EXECUTE
        OTHERS_READ -> PosixFileModeBit.OTHER_READ
        OTHERS_WRITE -> PosixFileModeBit.OTHER_WRITE
        OTHERS_EXECUTE -> PosixFileModeBit.OTHER_EXECUTE
    }
