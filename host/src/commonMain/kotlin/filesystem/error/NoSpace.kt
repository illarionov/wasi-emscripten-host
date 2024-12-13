/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.error

import at.released.weh.filesystem.model.FileSystemErrno

public data class NoSpace(
    override val message: String,
) : FileSystemOperationError,
    CloseError,
    FallocateError,
    HardlinkError,
    MkdirError,
    OpenError,
    RenameError,
    SymlinkError,
    SyncError,
    TruncateError,
    UnlinkError,
    WriteError {
    override val errno: FileSystemErrno = FileSystemErrno.NOSPC
}
