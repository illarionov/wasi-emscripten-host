/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.error

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.model.FileSystemErrno

@WasiEmscriptenHostDataModel
public class NoEntry(
    override val message: String,
) : FileSystemOperationError,
    CheckAccessError,
    ChmodError,
    ChownError,
    GetCurrentWorkingDirectoryError,
    MkdirError,
    OpenError,
    ReadDirError,
    RenameError,
    HardlinkError,
    ReadLinkError,
    SetTimestampError,
    StatError,
    SymlinkError,
    TruncateError,
    UnlinkError {
    override val errno: FileSystemErrno = FileSystemErrno.NOENT
}
