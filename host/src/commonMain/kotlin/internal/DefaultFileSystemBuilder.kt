/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.internal

import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemEngine
import at.released.weh.filesystem.dsl.FileSystemEngineConfig
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor
import at.released.weh.host.EmbedderHost.Builder

internal fun <E : FileSystemEngineConfig> Builder.thisOrCreateDefaultFileSystem(
    engine: FileSystemEngine<E>,
    fsLoggerTag: String,
): FileSystem {
    val builderFs = this.fileSystem
    return if (builderFs != null) {
        NoCloseFileSystemDecorator(builderFs)
    } else {
        createDefaultFileSystem(engine, fsLoggerTag)
    }
}

private fun <E : FileSystemEngineConfig> Builder.createDefaultFileSystem(
    engine: FileSystemEngine<E>,
    fsLoggerTag: String,
): FileSystem {
    val builder = this
    return FileSystem(engine) {
        addInterceptor(LoggingFileSystemInterceptor(builder.rootLogger.withTag(fsLoggerTag)))
        stdio {
            stdinProvider = builder.stdinProvider
            stdoutProvider = builder.stdoutProvider
            stderrProvider = builder.stderrProvider
        }
        directories {
            isRootAccessAllowed = builder.directoriesConfigBlock.isRootAccessAllowed
            currentWorkingDirectory = builder.directoriesConfigBlock.currentWorkingDirectory
            preopened {
                addAll(builder.directoriesConfigBlock.preopenedDirectories)
            }
        }
    }
}

private class NoCloseFileSystemDecorator(
    val delegate: FileSystem,
) : FileSystem by delegate {
    // Do not close delegate
    override fun close(): Unit = Unit
}
