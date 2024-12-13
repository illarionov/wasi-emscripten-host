/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.fdresource.stdio

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.IO

internal sealed interface StdioReadWriteError : FileSystemOperationError {
    data class Closed(
        override val message: String,
    ) : StdioReadWriteError {
        override val errno: FileSystemErrno = BADF
    }

    data class IoError(
        override val message: String,
    ) : StdioReadWriteError {
        override val errno: FileSystemErrno = IO
    }
}
