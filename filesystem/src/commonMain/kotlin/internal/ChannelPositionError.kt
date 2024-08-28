/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.Errno

internal sealed class ChannelPositionError(
    override val errno: Errno,
    override val message: String,
) : FileSystemOperationError {
    internal data class ClosedChannel(override val message: String) : ChannelPositionError(Errno.BADF, message)
    internal data class IoError(override val message: String) : ChannelPositionError(Errno.IO, message)
    internal data class InvalidArgument(override val message: String) : ChannelPositionError(Errno.INVAL, message)
}
