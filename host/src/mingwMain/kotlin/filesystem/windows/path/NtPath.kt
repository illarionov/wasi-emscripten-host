/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.path

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.ResolveRelativePathErrors
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.windows.WindowsRealPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtObjectManagerPath
import at.released.weh.filesystem.path.real.windows.nt.WindowsNtRelativePath
import at.released.weh.filesystem.path.toCommonError
import at.released.weh.filesystem.windows.win32api.filepath.GetFinalPathError
import at.released.weh.filesystem.windows.win32api.filepath.getFinalPath
import at.released.weh.filesystem.windows.win32api.filepath.toResolveRelativePathError
import platform.windows.HANDLE

internal sealed class NtPath {
    abstract val handle: HANDLE?
    abstract val pathString: String

    internal data class Absolute(
        val path: WindowsNtObjectManagerPath,
    ) : NtPath() {
        override val handle: HANDLE? get() = null
        override val pathString: String get() = path.kString
    }

    internal data class Relative(
        override val handle: HANDLE,
        val path: WindowsNtRelativePath = WindowsNtRelativePath.CURRENT,
    ) : NtPath() {
        override val pathString: String get() = path.kString
    }
}

internal fun NtPath.resolveAbsolutePath(): Either<ResolveRelativePathErrors, WindowsRealPath> = when (this) {
    is NtPath.Absolute -> if (path.kString.startsWith("""\??\""")) {
        WindowsRealPath.create(path.kString.replaceFirst("""\??\""", """\\?\"""))
    } else {
        WindowsRealPath.create(path.kString)
    }.mapLeft { it.toCommonError() }

    is NtPath.Relative -> either {
        val baseChannelPath = handle.getFinalPath()
            .mapLeft(GetFinalPathError::toResolveRelativePathError)
            .bind()
        return baseChannelPath.append(path.kString).mapLeft<ResolveRelativePathErrors>(PathError::toCommonError)
    }
}
