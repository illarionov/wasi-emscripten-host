/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.chmod

import at.released.weh.filesystem.error.ChmodError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

public data class ChmodFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,

    @FileMode
    public val mode: Int,
) {
    override fun toString(): String = "ChmodFd(fd=$fd, mode=0${mode.toString(8)}"

    public companion object : FileSystemOperation<ChmodFd, ChmodError, Unit> {
        override val tag: String = "chmodfd"
    }
}
