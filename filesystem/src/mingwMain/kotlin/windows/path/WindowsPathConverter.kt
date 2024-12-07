/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.path

import arrow.core.Either
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.path.virtual.ValidateVirtualPathError
import at.released.weh.filesystem.path.virtual.VirtualPath
import platform.windows.PathIsRelativeW

internal object WindowsPathConverter {
    /**
     * \\?\ Prefix disables string parsing and send string directly to file system.
     *
     * https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file
     */
    internal const val WIN32_LITERAL_FILE_NAMESPACE_PREFIX = """\\?\"""

    /**
     * The same as [WIN32_NT_KERNEL_DEVICES_PREFIX], but the path is normalized ('/' is replaced with '\', etc.)
     *
     */
    internal const val WIN32_NORMALIZED_FILE_NAMESPACE_PREFIX = """\\.\"""

    // https://stackoverflow.com/questions/23041983/path-prefixes-and/46019856#46019856
    internal const val WIN32_NT_KERNEL_DEVICES_PREFIX = """\??\"""

    internal fun convertToRealPath(path: VirtualPath): RealPath = convertPathToNtPath(path.toString())

    internal fun convertPathToNtPath(path: RealPath): RealPath {
        val winPathFull: String = path.replace('/', '\\')

        // XXX need own version without limit of MAX_PATH
        val pathIsRelative = PathIsRelativeW(winPathFull) != 0
        val winPath = winPathFull
            .substringAfter(WIN32_LITERAL_FILE_NAMESPACE_PREFIX)
            .substringAfter(WIN32_NORMALIZED_FILE_NAMESPACE_PREFIX)

        val ntPath = when {
            !pathIsRelative && !path.startsWith(WIN32_NT_KERNEL_DEVICES_PREFIX) ->
                WIN32_NT_KERNEL_DEVICES_PREFIX + winPath

            path == "." -> """"""
            else -> winPath
        }
        return ntPath
    }

    internal fun generatePreopenedDirectoryVirtualPath(
        realPath: RealPath,
    ): Either<ValidateVirtualPathError, VirtualPath> {
        val pathStripped = realPath
            .trim()
            .substringAfter(WIN32_LITERAL_FILE_NAMESPACE_PREFIX)
            .substringAfter(WIN32_NORMALIZED_FILE_NAMESPACE_PREFIX)
            .substringAfter(WIN32_NT_KERNEL_DEVICES_PREFIX)
            .replaceDriveLetter()

        val unixPath = pathStripped.replace('\\', '/')
        return VirtualPath.of(unixPath)
    }

    @Suppress("ComplexCondition")
    private fun String.replaceDriveLetter(): String = if (
        length >= 3 &&
        this[0].isValidVolumeName() &&
        this[1] == ':' &&
        (this[2] == '\\' || this[2] == '/')
    ) {
        "/${this[0]}/${this.drop(3)}"
    } else {
        this
    }

    private fun Char.isValidVolumeName() = this in 'a'..'z' || this in 'A'..'Z'
}
