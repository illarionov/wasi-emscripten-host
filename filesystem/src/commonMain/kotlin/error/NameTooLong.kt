/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.error

import at.released.weh.filesystem.model.FileSystemErrno

public data class NameTooLong(
    override val message: String,
) : FileSystemOperationError,
    CheckAccessError,
    ChmodError,
    ChownError,
    GetCurrentWorkingDirectoryError,
    MkdirError,
    OpenError,
    ReadLinkError,
    SetTimestampError,
    StatError,
    TruncateError,
    UnlinkError {
    override val errno: FileSystemErrno = FileSystemErrno.NAMETOOLONG
}
