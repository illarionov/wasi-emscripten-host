/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.poll

import kotlinx.cinterop.CValuesRef
import platform.posix.poll
import platform.posix.pollfd

internal actual fun nativePoll(fds: CValuesRef<pollfd>, nfds: Int, timeout: Int): Int =
    poll(fds, nfds.toULong(), timeout)
