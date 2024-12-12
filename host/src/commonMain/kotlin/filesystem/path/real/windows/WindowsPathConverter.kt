/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import arrow.core.getOrElse
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter.WIN32_NT_KERNEL_DEVICES_PREFIX
import at.released.weh.filesystem.path.virtual.VirtualPath

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

    // TODO: mingw style path converter
    internal fun convertToRealPath(path: VirtualPath): WindowsRealPath = convertPathToNtPath(path.toString())

    // TODO
    internal fun convertPathToNtPath(windowsPath: WindowsRealPath): WindowsNtRealPath {
        return windowsPath as? WindowsNtRealPath ?: convertPathToNtPath(windowsPath.kString)
    }

    internal fun convertPathToNtPath(windowsPath: String): WindowsNtRealPath {
        val winPathFull: String = windowsPath.replace('/', '\\')

        // XXX need own version without limit of MAX_PATH
        val pathIsRelative = !WindowsRealPath.windowsPathIsAbsolute(winPathFull)
        val winPath = winPathFull
            .substringAfter(WIN32_LITERAL_FILE_NAMESPACE_PREFIX)
            .substringAfter(WIN32_NORMALIZED_FILE_NAMESPACE_PREFIX)

        val ntPath = when {
            !pathIsRelative && !windowsPath.startsWith(WIN32_NT_KERNEL_DEVICES_PREFIX) ->
                WIN32_NT_KERNEL_DEVICES_PREFIX + winPath

            windowsPath == "." -> """"""
            else -> winPath
        }
        return WindowsNtRealPath.create(ntPath).getOrElse { error("Can not convert path") }
    }

    internal fun convertToVirtualPath(
        realPath: WindowsRealPath,
    ): Either<PathError, VirtualPath> {
        val pathStripped = realPath.kString
            .substringAfter(WIN32_LITERAL_FILE_NAMESPACE_PREFIX)
            .substringAfter(WIN32_NORMALIZED_FILE_NAMESPACE_PREFIX)
            .substringAfter(WIN32_NT_KERNEL_DEVICES_PREFIX)
            .replaceDriveLetter()

        val unixPath = pathStripped.replace('\\', '/')
        return VirtualPath.create(unixPath)
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
