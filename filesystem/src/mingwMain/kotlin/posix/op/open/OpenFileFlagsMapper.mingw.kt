/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.op.open

import at.released.weh.filesystem.op.opencreate.OpenFileFlag

internal actual val openFileFlagsMaskToPosixMask: List<Pair<Int, Int>> = listOf(
    OpenFileFlag.O_CREAT to platform.posix.O_CREAT,
    OpenFileFlag.O_EXCL to platform.posix.O_EXCL,
    OpenFileFlag.O_NOCTTY to 0,
    OpenFileFlag.O_TRUNC to platform.posix.O_TRUNC,
    OpenFileFlag.O_ASYNC to 0,
    OpenFileFlag.O_DIRECTORY to 0,
    OpenFileFlag.O_NOFOLLOW to 0,
    OpenFileFlag.O_CLOEXEC to 0,
)
