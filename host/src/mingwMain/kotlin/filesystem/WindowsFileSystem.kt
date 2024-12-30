/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.dsl.FileSystemCommonConfig
import at.released.weh.filesystem.posix.stdio.PosixStandardInputOutput
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.stdio.StdioSink
import at.released.weh.filesystem.stdio.StdioSource
import at.released.weh.filesystem.windows.WindowsFileSystemImpl

public object WindowsFileSystem : FileSystemEngine<Nothing> {
    @InternalWasiEmscriptenHostApi
    override fun create(
        commonConfig: FileSystemCommonConfig,
        engineConfig: Nothing.() -> Unit,
    ): FileSystem {
        val stdioConfig = commonConfig.stdioConfig
        val stdio = object : StandardInputOutput {
            override val stdinProvider: StdioSource.Provider =
                stdioConfig.stdinProvider ?: PosixStandardInputOutput.stdinProvider
            override val stdoutProvider: StdioSink.Provider =
                stdioConfig.stdoutProvider ?: PosixStandardInputOutput.stdoutProvider
            override val stderrProvider: StdioSink.Provider =
                stdioConfig.stderrProvider ?: PosixStandardInputOutput.stderrProvider
        }

        return WindowsFileSystemImpl(
            interceptors = commonConfig.interceptors,
            stdio = stdio,
            isRootAccessAllowed = commonConfig.directoryConfig.isRootAccessAllowed,
            currentWorkingDirectory = commonConfig.directoryConfig.currentWorkingDirectory,
            preopenedDirectories = commonConfig.directoryConfig.preopenedDirectories,
        )
    }
}
