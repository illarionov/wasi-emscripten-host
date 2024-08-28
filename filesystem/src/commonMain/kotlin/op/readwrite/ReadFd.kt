/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readwrite

import at.released.weh.filesystem.error.ReadError
import at.released.weh.filesystem.model.Fd
import at.released.weh.filesystem.op.FileSystemOperation

public data class ReadFd(
    public val fd: Fd,
    public val iovecs: List<FileSystemByteBuffer>,
    public val strategy: ReadWriteStrategy = ReadWriteStrategy.CHANGE_POSITION,
) {
    public companion object : FileSystemOperation<ReadFd, ReadError, ULong> {
        override val tag: String = "readfd"
    }
}
