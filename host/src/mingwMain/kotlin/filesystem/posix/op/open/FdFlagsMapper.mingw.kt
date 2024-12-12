/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.op.open

import at.released.weh.filesystem.model.FdFlag

internal actual val fsFdFlagsMaskToPosixMask: List<Pair<Int, Int>> = listOf(
    FdFlag.FD_APPEND to platform.posix.O_APPEND,
    FdFlag.FD_NONBLOCK to 0,
    FdFlag.FD_DSYNC to 0,
    FdFlag.FD_SYNC to 0,
    FdFlag.FD_RSYNC to 0,
)
