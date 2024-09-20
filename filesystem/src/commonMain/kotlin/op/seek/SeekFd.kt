/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.seek

import at.released.weh.filesystem.error.SeekError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.model.Whence
import at.released.weh.filesystem.op.FileSystemOperation

public data class SeekFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,
    public val fileDelta: Long,
    public val whence: Whence,
) {
    public companion object : FileSystemOperation<SeekFd, SeekError, Long> {
        override val tag: String = "seekfd"
    }
}
