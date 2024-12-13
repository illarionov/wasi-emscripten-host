/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")

package at.released.weh.filesystem.path.virtual

import arrow.core.Either
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.real.posix.PosixPathValidator
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.isNotEmpty
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.jvm.JvmStatic

/**
 * Path within the virtual file system.
 *
 * Path can be expressed as a sequence of Unicode Scalar Values (USVs). It can be either absolute or relative.
 * The underlying file system may impose restrictions on the path length and the set of allowed characters.
 *
 * The directory separator is always the forward-slash (`/`).
 *
 * This class provides only basic validity and structural correctness of the path's byte representation (similar to
 * validating a query string). It does not guarantee that the path exists on a specific file system, nor that querying
 * the file system using this path will success.
 */
public class VirtualPath private constructor(
    /**
     * UTF-8 representation of path. Not null terminated.
     */
    public val utf8: ByteString,
) {
    private val utf8String: String by lazy(NONE) {
        utf8.decodeToString()
    }

    /**
     * Number of bytes to represent the path in UTF8. Not null terminated
     */
    public val utf8SizeBytes: Int = utf8.size

    init {
        check(utf8.isNotEmpty())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as VirtualPath

        return utf8 == other.utf8
    }

    override fun toString(): String = utf8String

    override fun hashCode(): Int {
        return utf8.hashCode()
    }

    public companion object {
        @JvmStatic
        public fun create(string: String): Either<PathError, VirtualPath> {
            return PosixPathValidator.validate(string).map {
                VirtualPath(string.encodeToByteString())
            }
        }

        internal fun create(bytes: ByteString): Either<PathError, VirtualPath> {
            return PosixPathValidator.validate(bytes).map {
                VirtualPath(bytes)
            }
        }

        public fun VirtualPath.isDirectoryRequest(): Boolean = utf8String.last() == '/'
        public fun VirtualPath.isAbsolute(): Boolean = utf8String.first() == '/'
    }
}
