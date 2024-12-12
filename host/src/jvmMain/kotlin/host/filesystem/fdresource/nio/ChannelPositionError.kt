/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.IO

@InternalWasiEmscriptenHostApi
public sealed class ChannelPositionError(
    override val errno: FileSystemErrno,
    override val message: String,
) : FileSystemOperationError {
    @InternalWasiEmscriptenHostApi
    public data class ClosedChannel(override val message: String) : ChannelPositionError(BADF, message)

    @InternalWasiEmscriptenHostApi
    public data class IoError(override val message: String) : ChannelPositionError(IO, message)

    @InternalWasiEmscriptenHostApi
    public data class InvalidArgument(override val message: String) : ChannelPositionError(INVAL, message)
}
