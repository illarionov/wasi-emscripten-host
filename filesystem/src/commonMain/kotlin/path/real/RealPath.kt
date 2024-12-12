/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real

import arrow.core.Either
import at.released.weh.filesystem.path.PathError
import kotlinx.io.bytestring.ByteString

/**
 * Represents a platform-specific path on the real file system.
 *
 * Path can be either absolute or relative. Is is not guaranteed to be a valid Unicode string.
 * The rules for valid paths and the set of acceptable characters depend on the underlying platform, but we assume
 * the following general restriction:
 *   * Null character (`'\0'`) is not allowed in a path.
 *
 * This class provides only basic validity and structural correctness of the path's byte representation (similar to
 * validating a query string). It does not guarantee that the path exists on a specific file system, nor that querying
 * the file system using this path will success.
 */
internal interface RealPath {
    /**
     * Not null terminated UTF-8 representation of path.
     */
    val utf8Bytes: ByteString

    /**
     * The string representation of this path.
     */
    fun decodeToString(): Either<PathError.InvalidPathFormat, String>

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int

    interface Factory<out R : RealPath> {
        fun create(bytes: ByteString): Either<PathError, R>
        fun create(path: String): Either<PathError, R>
    }
}
