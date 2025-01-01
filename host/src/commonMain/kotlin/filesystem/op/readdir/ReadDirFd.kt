/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readdir

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

@WasiEmscriptenHostDataModel
public class ReadDirFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,
    public val startPosition: DirSequenceStartPosition = DirSequenceStartPosition.Start,
) {
    public sealed class DirSequenceStartPosition {
        public data object Start : DirSequenceStartPosition()

        @WasiEmscriptenHostDataModel
        public class Cookie(public val cookie: Long) : DirSequenceStartPosition()
    }

    public companion object : FileSystemOperation<ReadDirFd, ReadDirError, DirEntrySequence> {
        override val tag: String = "readdirfd"
    }
}
