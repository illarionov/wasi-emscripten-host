/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.path.PathError

internal fun normalizeWindowsPath(
    path: String,
    failOnOutsideOfRoot: Boolean = true,
): Either<PathError, String> {
    val type = getWindowsPathType(path)
    val prefixSize = type.prefixLength

    val prefix = path.substring(startIndex = 0, endIndex = prefixSize).replace('/', '\\')
    if (prefixSize == path.length) {
        return prefix.right()
    }

    val components: MutableList<String> = mutableListOf()
    path.substring(prefixSize).split('/', '\\').forEach { component ->
        when (component) {
            "", "." -> Unit
            ".." -> if (components.isNotEmpty()) {
                components.removeLast()
            } else {
                if (failOnOutsideOfRoot) {
                    return PathError.PathOutsideOfRootPath("Path was outside the root during normalization").left()
                }
            }

            else -> components.add(component)
        }
    }
    if (components.isNotEmpty()) {
        components[components.lastIndex] = components.last().trimTrailingDotsSpaces()
    }

    return buildString {
        components.joinTo(this, "\\", prefix = prefix)
        if ((path.endsWith("\\") || path.endsWith("/")) && !this.endsWith("\\")) {
            append("\\")
        }
    }.right()
}

private fun String.trimTrailingDotsSpaces(): String {
    val searchRange = indices.reversed()
    if (searchRange.isEmpty()) {
        return this
    }
    val lastNonSpace = searchRange.find {
        val ch = this[it]
        ch != ' ' && ch != '.'
    }
    return when (lastNonSpace) {
        null -> this
        else -> this.substring(0, lastNonSpace + 1)
    }
}
