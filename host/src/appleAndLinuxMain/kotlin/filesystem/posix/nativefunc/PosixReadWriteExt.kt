/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.nativefunc

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.op.readwrite.FileSystemByteBuffer
import at.released.weh.filesystem.posix.NativeFileFd
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pin
import platform.posix.NULL
import platform.posix.errno
import platform.posix.iovec

internal fun callReadWrite(
    fd: NativeFileFd,
    iovecs: List<FileSystemByteBuffer>,
    block: (fd: NativeFileFd, iovecs: CArrayPointer<iovec>, size: Int) -> Long,
): Either<Int, ULong> {
    val bytesMoved = memScoped {
        val size = iovecs.size
        val posixIovecs: CArrayPointer<iovec> = allocArray(size)
        iovecs.withPinnedByteArrays { pinnedByteArrays: List<Pinned<ByteArray>?> ->
            iovecs.forEachIndexed { index, vec ->
                posixIovecs[index].apply {
                    val pinnedByteArray = pinnedByteArrays[index]
                    if (pinnedByteArray != null) {
                        iov_base = pinnedByteArray.addressOf(vec.offset)
                        iov_len = vec.length.toULong()
                    } else {
                        iov_base = NULL
                        iov_len = 0U
                    }
                }
            }
            block(fd, posixIovecs, size)
        }
    }

    return if (bytesMoved >= 0) {
        bytesMoved.toULong().right()
    } else {
        errno.left()
    }
}

private inline fun <R : Any> List<FileSystemByteBuffer>.withPinnedByteArrays(
    block: (byteArrays: List<Pinned<ByteArray>?>) -> R,
): R {
    val pinnedByteArrays: List<Pinned<ByteArray>?> = this.map {
        @Suppress("ReplaceSizeCheckWithIsNotEmpty")
        if (it.array.size != 0) {
            it.array.pin()
        } else {
            null
        }
    }
    return try {
        block(pinnedByteArrays)
    } finally {
        pinnedByteArrays.filterNotNull().forEach(Pinned<ByteArray>::unpin)
    }
}
