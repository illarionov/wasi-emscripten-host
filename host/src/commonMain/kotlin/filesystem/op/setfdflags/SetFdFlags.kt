/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.setfdflags

import at.released.weh.filesystem.error.SetFdFlagsError
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import at.released.weh.filesystem.op.FileSystemOperation

/**
 * Sets the status flags of an open file descriptor [fd] to [flags].
 * This is similar to `fcntl(fd, F_SETFL, flags)` in POSIX.
 */
public data class SetFdFlags(
    @IntFileDescriptor
    public val fd: FileDescriptor,

    @FdflagsType
    val flags: Fdflags,
) {
    public companion object : FileSystemOperation<SetFdFlags, SetFdFlagsError, Unit> {
        override val tag: String = "setfdflags"
    }
}
