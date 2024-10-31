/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.error

import at.released.weh.filesystem.model.FileSystemErrno

public data class IoError(
    override val message: String,
) : FileSystemOperationError,
    AdvisoryLockError,
    CheckAccessError,
    ChmodError,
    ChownError,
    CloseError,
    FdAttributesError,
    OpenError,
    MkdirError,
    ReadDirError,
    ReadError,
    ReadLinkError,
    SetTimestampError,
    StatError,
    SymlinkError,
    SyncError,
    TruncateError,
    UnlinkError,
    WriteError {
    override val errno: FileSystemErrno = FileSystemErrno.IO
}
