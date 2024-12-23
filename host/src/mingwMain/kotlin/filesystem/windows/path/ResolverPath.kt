/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.path

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.windows.normalizeWindowsPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtObjectManagerPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest
import at.released.weh.filesystem.windows.win32api.createfile.NtPath
import platform.windows.HANDLE

internal sealed class ResolverPath {
    data class AbsoluteNtPath(
        val path: WindowsNtObjectManagerPath,
    ) : ResolverPath()

    data class RelativePath(
        val handle: HANDLE,
        val path: VirtualPath,
    ) : ResolverPath()
}

internal fun ResolverPath.isDirectoryRequest(): Boolean = when (this) {
    is ResolverPath.AbsoluteNtPath -> path.kString.endsWith("\\")
    is ResolverPath.RelativePath -> path.isDirectoryRequest()
}

internal fun ResolverPath.toNtPath(): Either<PathError, NtPath> = when (this) {
    is ResolverPath.AbsoluteNtPath -> NtPath.Absolute(this.path).right()
    is ResolverPath.RelativePath -> this.path.toNtPathRelativePart().map { NtPath.Relative(this.handle, it) }
}

private fun VirtualPath.toNtPathRelativePart(): Either<PathError, WindowsNtRelativePath> {
    return normalizeWindowsPath(toString())
        .flatMap { canonizedPath -> WindowsNtRelativePath.create(canonizedPath) }
}
