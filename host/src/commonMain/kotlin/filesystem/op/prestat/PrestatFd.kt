/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.prestat

import at.released.weh.filesystem.error.PrestatError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

/**
 * Return a path of the given preopened file descriptor.
 */
public data class PrestatFd(
    @IntFileDescriptor
    val fd: FileDescriptor,
) {
    public companion object : FileSystemOperation<PrestatFd, PrestatError, PrestatResult> {
        override val tag: String = "prestatfd"
    }
}
