/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix

import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.DiskQuota
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.IntFileDescriptor
import platform.posix.EDQUOT

internal actual fun Int.platformSpecificErrnoToCloseError(@IntFileDescriptor fd: FileDescriptor): CloseError {
    return when (this) {
        EDQUOT -> DiskQuota("Disk quota exceeded while closing $fd")
        else -> IoError("Unknown error $this while closing $fd")
    }
}
