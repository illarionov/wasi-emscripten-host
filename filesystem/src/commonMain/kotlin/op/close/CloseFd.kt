/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.close

import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.wasi.filesystem.common.Fd

public data class CloseFd(
    @Fd
    public val fd: Int,
) {
    public companion object : FileSystemOperation<CloseFd, CloseError, Unit> {
        override val tag: String = "closefd"
    }
}
