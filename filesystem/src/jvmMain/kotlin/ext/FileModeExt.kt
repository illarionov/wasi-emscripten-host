/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.ext

import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.FileModeFlag.S_IRGRP
import at.released.weh.filesystem.model.FileModeFlag.S_IROTH
import at.released.weh.filesystem.model.FileModeFlag.S_IRUSR
import at.released.weh.filesystem.model.FileModeFlag.S_IWGRP
import at.released.weh.filesystem.model.FileModeFlag.S_IWOTH
import at.released.weh.filesystem.model.FileModeFlag.S_IWUSR
import at.released.weh.filesystem.model.FileModeFlag.S_IXGRP
import at.released.weh.filesystem.model.FileModeFlag.S_IXOTH
import at.released.weh.filesystem.model.FileModeFlag.S_IXUSR
import java.nio.file.FileSystem
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

internal fun FileSystem.fileModeAsFileAttributesIfSupported(
    @FileMode mode: Int,
): Array<FileAttribute<*>> = if (supportedFileAttributeViews().contains("posix")) {
    arrayOf(PosixFilePermissions.asFileAttribute(mode.fileModeToPosixFilePermissions()))
} else {
    emptyArray()
}

@Suppress("NO_BRACES_IN_CONDITIONALS_AND_LOOPS")
internal fun Int.fileModeToPosixFilePermissions(): Set<PosixFilePermission> {
    val permissions: MutableSet<PosixFilePermission> = mutableSetOf()

    if (this and S_IRUSR != 0) permissions += PosixFilePermission.OWNER_READ
    if (this and S_IWUSR != 0) permissions += PosixFilePermission.OWNER_WRITE
    if (this and S_IXUSR != 0) permissions += PosixFilePermission.OWNER_EXECUTE

    if (this and S_IRGRP != 0) permissions += PosixFilePermission.GROUP_READ
    if (this and S_IWGRP != 0) permissions += PosixFilePermission.GROUP_WRITE
    if (this and S_IXGRP != 0) permissions += PosixFilePermission.GROUP_EXECUTE

    if (this and S_IROTH != 0) permissions += PosixFilePermission.OTHERS_READ
    if (this and S_IWOTH != 0) permissions += PosixFilePermission.OTHERS_WRITE
    if (this and S_IXOTH != 0) permissions += PosixFilePermission.OTHERS_EXECUTE

    return permissions
}
