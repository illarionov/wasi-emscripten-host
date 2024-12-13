/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.truncate

import at.released.weh.filesystem.error.TruncateError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

/**
 * Truncate or extend a file [fd] to a specified [length].
 * If the file was larger than this size, the extra data will be discarded.
 * If the file was smaller than this size, it will be extended as if by writing bytes with the value zero.
 *
 * The file offset is not changed.
 */
public data class TruncateFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,
    public val length: Long,
) {
    public companion object : FileSystemOperation<TruncateFd, TruncateError, Unit> {
        override val tag: String = "truncatefd"
    }
}
