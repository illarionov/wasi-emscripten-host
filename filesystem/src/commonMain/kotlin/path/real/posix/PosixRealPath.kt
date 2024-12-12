/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.posix

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.RealPath
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import kotlin.LazyThreadSafetyMode.PUBLICATION

/**
 * Represents a Path in Unix-like File Systems
 *
 * Although Unix paths on some operating systems may not be a valid Unicode strings, this implementation only supports
 * valid Unicode paths.
 *
 * General Path Characteristics:
 *   * The directory separator is always a forward-slash (`/`)
 *   * Paths must be valid Unicode string
 *   * A null character (`\0`) is not allowed in a path.
 *   * An empty string is considered an invalid path
 *   * Absolute path is a path that starts with a forward-slash
 *   * The `.` entry refers to the current directory, while `..` refers to the parent directory
 *   * The maximum path length depends on the operating system and the file system.
 *
 * @param utf8Bytes UTF-8 representation of path. Not null terminated.
 */
internal class PosixRealPath private constructor(
    override val utf8Bytes: ByteString,
) : RealPath {
    public val kString: String by lazy(PUBLICATION) {
        utf8Bytes.decodeToString()
    }

    override fun decodeToString(): Either<PathError.InvalidPathFormat, String> {
        return kString.right()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as PosixRealPath

        return utf8Bytes == other.utf8Bytes
    }

    override fun hashCode(): Int {
        return utf8Bytes.hashCode()
    }

    override fun toString(): String = kString

    internal companion object : RealPath.Factory<PosixRealPath> {
        override fun create(bytes: ByteString): Either<PathError, PosixRealPath> {
            return Either.catch { bytes.toByteArray().decodeToString(throwOnInvalidSequence = true) }
                .mapLeft { _ -> PathError.InvalidPathFormat("Path is not a valid Unicode string") }
                .flatMap<PathError, String, PosixRealPath>(::create)
        }

        override fun create(path: String): Either<PathError, PosixRealPath> = PosixPathValidator.validate(path)
            .map { PosixRealPath(path.encodeToByteString()) }
    }
}
