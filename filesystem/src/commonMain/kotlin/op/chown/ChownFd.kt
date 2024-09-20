/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.chown

import at.released.weh.filesystem.error.ChownError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

public data class ChownFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,
    public val owner: Int,
    public val group: Int,
) {
    public companion object : FileSystemOperation<ChownFd, ChownError, Unit> {
        override val tag: String = "chownfd"
    }
}
