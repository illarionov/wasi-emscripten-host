/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.stdio

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.stdio.SinkProvider
import kotlinx.io.RawSink
import platform.posix.STDERR_FILENO
import platform.posix.STDOUT_FILENO

internal class PosixSinkProvider(
    private val fd: FileDescriptor,
) : SinkProvider {
    override fun open(): RawSink {
        return PosixFdSink.create(fd)
    }

    internal companion object {
        val stdoutProvider: SinkProvider = PosixSinkProvider(fd = STDOUT_FILENO)
        val stderrProvider: SinkProvider = PosixSinkProvider(fd = STDERR_FILENO)
    }
}
