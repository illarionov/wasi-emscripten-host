/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readwrite

public class FileSystemByteBuffer(
    public val array: ByteArray,
    public val offset: Int = 0,
    public val length: Int = array.size,
) {
    init {
        require(array.size >= 0)
        @Suppress("ReplaceSizeCheckWithIsNotEmpty")
        if (array.size > 0) {
            require(offset in array.indices)
            require(offset + length in 0..array.size)
        } else {
            require(offset == 0)
            require(length == 0)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || this::class != other::class) {
            return false
        }

        other as FileSystemByteBuffer

        if (!array.contentEquals(other.array)) {
            return false
        }
        if (offset != other.offset) {
            return false
        }
        if (length != other.length) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = array.contentHashCode()
        result = 31 * result + offset
        result = 31 * result + length
        return result
    }

    override fun toString(): String {
        return "FileSystemByteBuffer(array.size=${array.size}, offset=$offset, length=$length)"
    }
}
