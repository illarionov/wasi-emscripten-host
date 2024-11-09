/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.op.open

import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_CREAT
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_TMPFILE
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType

@Suppress("MagicNumber")
internal fun getFileOpenModeConsideringOpenFlags(
    @OpenFileFlagsType flags: OpenFileFlags,
    @FileMode mode: Int?,
): Int = when {
    (flags and O_CREAT != O_CREAT) && (flags and O_TMPFILE != O_TMPFILE) -> 0
    mode != null -> mode
    else -> 0b110_100_000
}
