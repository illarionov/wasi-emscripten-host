/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

/**
 * Retrieves information about the currently open file [fd].
 */
@WasiEmscriptenHostDataModel
public class StatFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,
) {
    public companion object : FileSystemOperation<StatFd, StatError, StructStat> {
        override val tag: String = "statfd"
    }
}
