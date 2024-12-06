/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api.ext

import at.released.weh.filesystem.preopened.RealPath
import platform.windows.PathIsRelativeW

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

// TODO: normalize path?
internal fun combinePath(root: String, child: String): String {
    val rootNormalized = root.replace('/', '\\')
    if (child.isEmpty()) {
        return rootNormalized
    }
    val childNormalized = child.replace('/', '\\')

    return if (rootNormalized.endsWith("\\")) {
        """$rootNormalized$childNormalized"""
    } else {
        """$rootNormalized\$childNormalized"""
    }
}

internal fun convertUnixPathToWindowsPath(
    path: String
): String {
    return convertPathToNtPath(path)
}

internal fun convertPathToNtPath(
    path: RealPath,
): RealPath {
    val pathIsRelative = PathIsRelativeW(path) != 0 // XXX need own version without limit of MAX_PATH
    val winPath = path.replace('/', '\\')
        .substringAfter(WIN32_LITERAL_FILE_NAMESPACE_PREFIX)
        .substringAfter(WIN32_NORMALIZED_FILE_NAMESPACE_PREFIX)

    val ntPath = when {
        !pathIsRelative && !path.startsWith(WIN32_NT_KERNEL_DEVICES_PREFIX) -> WIN32_NT_KERNEL_DEVICES_PREFIX + winPath
        path == "." -> """"""
        else -> winPath
    }
    return ntPath
}
