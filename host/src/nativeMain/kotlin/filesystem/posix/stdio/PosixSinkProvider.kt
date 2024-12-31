/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.stdio

import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.stdio.StdioSink
import platform.posix.STDERR_FILENO
import platform.posix.STDOUT_FILENO

internal class PosixSinkProvider(
    private val fd: NativeFileFd,
) : StdioSink.Provider {
    override fun open(): StdioSink {
        return PosixFdSink.create(fd)
    }

    internal companion object {
        val stdoutProvider: StdioSink.Provider = PosixSinkProvider(fd = NativeFileFd(STDOUT_FILENO))
        val stderrProvider: StdioSink.Provider = PosixSinkProvider(fd = NativeFileFd(STDERR_FILENO))
    }
}
