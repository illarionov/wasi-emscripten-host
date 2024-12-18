/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.RealPath
import at.released.weh.filesystem.path.real.windows.WindowsPathType.LOCAL_DEVICE_LITERAL
import at.released.weh.filesystem.path.real.windows.WindowsPathType.ROOT_LOCAL_DEVICE
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * Represents a Windows path.
 */
internal class WindowsRealPath private constructor(
    internal val kString: String,
) : RealPath {
    val type: WindowsPathType = getWindowsPathType(kString)
    override val utf8Bytes: ByteString by lazy(PUBLICATION) { kString.encodeToByteString() }
    val isDirectoryRequest: Boolean = kString.lastOrNull()?.let(::isPathSeparator) == true

    /**
     * Returns parent directory or `null` if there is no parent directory for this path
     */
    public val parent: WindowsRealPath?
        get() {
            if (kString.length <= 1) {
                return null
            }
            val lastSlash =
                (kString.lastIndex - 1 downTo this.type.prefixLength - 1).find { isPathSeparator(kString[it]) }
            return lastSlash?.let { endSlashPosition -> WindowsRealPath(kString.substring(0, endSlashPosition + 1)) }
        }

    override fun decodeToString(): Either<PathError.InvalidPathFormat, String> = kString.right()

    internal fun isPathSeparator(char: Char): Boolean = when (type) {
        LOCAL_DEVICE_LITERAL, ROOT_LOCAL_DEVICE -> char == '\\'
        else -> char == '\\' || char == '/'
    }

    fun normalize(): Either<PathError, WindowsRealPath> = normalizeWindowsPath(kString).map(::WindowsRealPath)

    fun append(
        part: String,
        normalizePath: Boolean = true,
    ): Either<PathError, WindowsRealPath> {
        return "$kString\\$part".let {
            if (normalizePath) {
                normalizeWindowsPath(it)
            } else {
                it.right()
            }
        }.flatMap { pathString -> create(pathString) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as WindowsRealPath

        return kString == other.kString
    }

    override fun hashCode(): Int {
        return kString.hashCode()
    }

    override fun toString(): String = kString

    public companion object : RealPath.Factory<WindowsRealPath> {
        internal val WindowsRealPath.pathPrefix: String get() = this.kString.substring(0, this.type.prefixLength)
        internal val WindowsRealPath.pathNoPrefix: String get() = this.kString.substring(this.type.prefixLength)

        override fun create(bytes: ByteString): Either<PathError, WindowsRealPath> {
            return Either.catch { bytes.toByteArray().decodeToString(throwOnInvalidSequence = true) }
                .mapLeft { PathError.InvalidPathFormat("Path is not a valid Unicode string") }
                .flatMap<PathError, String, WindowsRealPath>(::create)
        }

        override fun create(path: String): Either<PathError, WindowsRealPath> {
            return WindowsRealPath(path).right()
        }
    }
}
