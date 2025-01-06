/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemEngine
import at.released.weh.filesystem.dsl.FileSystemCommonConfig
import at.released.weh.filesystem.stdio.JvmStandardInputOutput
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.stdio.StdioSink
import at.released.weh.filesystem.stdio.StdioSource

public object NioFileSystem : FileSystemEngine<NioFileSystemConfig> {
    @InternalWasiEmscriptenHostApi
    override fun create(
        commonConfig: FileSystemCommonConfig,
        engineConfig: NioFileSystemConfig.() -> Unit,
    ): FileSystem {
        val nioConfig = NioFileSystemConfig().apply(engineConfig)

        val stdioConfig = commonConfig.stdioConfig
        val stdio = JvmLocalStandardInputOutput(
            stdinProvider = stdioConfig.stdinProvider ?: JvmStandardInputOutput.stdinProvider,
            stdoutProvider = stdioConfig.stdoutProvider ?: JvmStandardInputOutput.stdoutProvider,
            stderrProvider = stdioConfig.stderrProvider ?: JvmStandardInputOutput.stderrProvider,
        )

        return NioFileSystemImpl(
            javaFs = nioConfig.nioFileSystem,
            interceptors = commonConfig.interceptors,
            stdio = stdio,
            isRootAccessAllowed = commonConfig.unrestricted,
            currentWorkingDirectory = commonConfig.currentWorkingDirectory,
            preopenedDirectories = commonConfig.preopenedDirectories,
        )
    }

    private data class JvmLocalStandardInputOutput(
        override val stdinProvider: StdioSource.Provider,
        override val stdoutProvider: StdioSink.Provider,
        override val stderrProvider: StdioSink.Provider,
    ) : StandardInputOutput
}
