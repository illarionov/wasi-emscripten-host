/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.dsl.FileSystemCommonConfig
import at.released.weh.filesystem.linux.LinuxFileSystemImpl
import at.released.weh.filesystem.posix.stdio.PosixStandardInputOutput
import at.released.weh.filesystem.stdio.SinkProvider
import at.released.weh.filesystem.stdio.SourceProvider
import at.released.weh.filesystem.stdio.StandardInputOutput

public object LinuxFileSystem : FileSystemEngine<Nothing> {
    @InternalWasiEmscriptenHostApi
    override fun create(
        commonConfig: FileSystemCommonConfig,
        engineConfig: Nothing.() -> Unit,
    ): FileSystem {
        val stdioConfig = commonConfig.stdioConfig
        val stdio = object : StandardInputOutput {
            override val stdinProvider: SourceProvider =
                stdioConfig.stdinProvider ?: PosixStandardInputOutput.stdinProvider
            override val stdoutProvider: SinkProvider =
                stdioConfig.stdoutProvider ?: PosixStandardInputOutput.stdoutProvider
            override val stderrProvider: SinkProvider =
                stdioConfig.stderrProvider ?: PosixStandardInputOutput.stderrProvider
        }

        return LinuxFileSystemImpl(
            interceptors = commonConfig.interceptors,
            stdio = stdio,
            isRootAccessAllowed = commonConfig.directoryConfig.isRootAccessAllowed,
            currentWorkingDirectory = commonConfig.directoryConfig.currentWorkingDirectory,
            preopenedDirectories = commonConfig.directoryConfig.preopenedDirectories,
        )
    }
}
