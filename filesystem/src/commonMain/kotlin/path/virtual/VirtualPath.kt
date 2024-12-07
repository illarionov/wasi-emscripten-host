/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")

package at.released.weh.filesystem.path.virtual

import arrow.core.Either
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

    override fun toString(): String = utf8String

    public companion object {
        @JvmStatic
        public fun of(string: String): Either<ValidateVirtualPathError, VirtualPath> {
            val utf8ByteString = string.encodeToByteString()
            return VirtualPathValidator.validate(utf8ByteString).map {
                VirtualPath(utf8ByteString)
            }
        }

        internal fun VirtualPath.isDirectoryRequest() = utf8String.last() == '/'
        internal fun VirtualPath.isAbsolute() = utf8String.first() == '/'
    }
}
