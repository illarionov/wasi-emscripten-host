/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.fadvise

import at.released.weh.filesystem.error.FadviseError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

public data class FadviseFd(
    @IntFileDescriptor
    val fd: FileDescriptor,
    val offset: Long,
    val length: Long,
    val advice: Advice,
) {
    public companion object : FileSystemOperation<FadviseFd, FadviseError, Unit> {
        override val tag: String = "fadvisefd"
    }
}
