/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readwrite

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.WriteError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

@WasiEmscriptenHostDataModel
public class WriteFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,
    public val cIovecs: List<FileSystemByteBuffer>,
    public val strategy: ReadWriteStrategy = ReadWriteStrategy.CurrentPosition,
) {
    public companion object : FileSystemOperation<WriteFd, WriteError, ULong> {
        override val tag: String = "writefd"
    }
}
