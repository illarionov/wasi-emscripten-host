/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.ext

import at.released.weh.wasi.filesystem.common.Whence
import at.released.weh.wasi.filesystem.common.Whence.CUR
import at.released.weh.wasi.filesystem.common.Whence.END
import at.released.weh.wasi.filesystem.common.Whence.SET
import platform.posix.SEEK_CUR
import platform.posix.SEEK_END
import platform.posix.SEEK_SET

internal fun Whence.toPosixWhence(): Int = when (this) {
    SET -> SEEK_SET
    CUR -> SEEK_CUR
    END -> SEEK_END
}
