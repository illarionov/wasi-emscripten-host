/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.fdrenumber

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.FdrenumberError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

@WasiEmscriptenHostDataModel
public class Fdrenumber(
    public val fromFd: FileDescriptor,
    public val toFd: FileDescriptor,
) {
    public companion object : FileSystemOperation<Fdrenumber, FdrenumberError, Unit> {
        override val tag: String = "fdrenumber"
    }
}
