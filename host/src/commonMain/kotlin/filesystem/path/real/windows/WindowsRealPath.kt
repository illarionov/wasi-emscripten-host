/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.RealPath
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.files.Path
import kotlin.LazyThreadSafetyMode.PUBLICATION

internal expect val hasNativeWindowsPathFunctions: Boolean

internal expect fun nativeWindowsPathIsRelative(path: String): Boolean

internal sealed class WindowsRealPath protected constructor(
    protected val utf16Chars: CharArray,
) : RealPath {
    internal val kString: String by lazy(PUBLICATION) {
        utf16Chars.concatToString()
    }
    override val utf8Bytes: ByteString by lazy(PUBLICATION) {
        kString.encodeToByteString()
    }
    open val isAbsolute: Boolean by lazy(PUBLICATION) {
        windowsPathIsAbsolute(kString)
    }
    open val isDirectoryRequest: Boolean = kString.lastOrNull().let {
        it == '\\' || it == '/'
    }

    /**
     * Returns parent directory or `null` if there is no parent directory for this path
     */
    public val parent: WindowsRealPath?
        // TODO
        get() = Path(kString).parent?.let { create(it.toString()).getOrNull() }

    override fun decodeToString(): Either<PathError.InvalidPathFormat, String> {
        return kString.right()
    }

    public companion object : RealPath.Factory<WindowsRealPath> {
        override fun create(bytes: ByteString): Either<PathError, WindowsRealPath> {
            return Either.catch { bytes.toByteArray().decodeToString(throwOnInvalidSequence = true) }
                .mapLeft { PathError.InvalidPathFormat("Path is not a valid Unicode string") }
                .flatMap<PathError, String, WindowsRealPath>(::create)
        }

        internal fun create(base: WindowsRealPath, vararg parts: WindowsRealPath): Either<PathError, WindowsRealPath> {
            if (parts.any { windowsPathIsAbsolute(it.kString) }) {
                return PathError.InvalidPathFormat("Can not concatenate absolute path part").left()
            }
            return create(
                base = base,
                parts = Array<String>(parts.size) { index -> parts[index].kString },
            )
        }

        internal fun create(base: WindowsRealPath, vararg parts: String): Either<PathError, WindowsRealPath> {
            val path = buildString {
                append(base.kString)
                if (this.last() != '\\' && this.last() != '/') {
                    append('\\')
                }
                parts
                    .map { it.removePrefix("\\").removePrefix("/") }
                    .filter(String::isNotEmpty)
                    .joinTo(this, separator = "\\", prefix = "") { it }
            }
            return create(path)
        }

        override fun create(path: String): Either<PathError, WindowsRealPath> {
            // TODO
            return WindowsPathValidator.validate(path)
                .map {
                    WindowsCommonRealPath.createRawUnsafe(path.toCharArray())
                }
        }

        internal fun windowsPathIsAbsolute(path: String): Boolean {
            return if (hasNativeWindowsPathFunctions) {
                !nativeWindowsPathIsRelative(path)
            } else {
                commonWindowsPathIsRelative(path)
            }
        }

        @Suppress("FunctionOnlyReturningConstant", "UnusedParameter")
        private fun commonWindowsPathIsRelative(path: String): Boolean {
            // TODO
            return true
        }
    }
}
