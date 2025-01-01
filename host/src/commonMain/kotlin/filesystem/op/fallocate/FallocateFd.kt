/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.fallocate

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.error.FallocateError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

/**
 * Reserve storage for a specified range within a file, starting at [offset] and extending for [length],
 * on the file descriptor [fd].
 * This functions behaves similarly to `posix_fallocate` in POSIX.
 */
@WasiEmscriptenHostDataModel
public class FallocateFd(
    @IntFileDescriptor
    public val fd: FileDescriptor,
    public val offset: Long,
    public val length: Long,
) {
    public companion object : FileSystemOperation<FallocateFd, FallocateError, Unit> {
        override val tag: String = "fallocate"
    }
}
