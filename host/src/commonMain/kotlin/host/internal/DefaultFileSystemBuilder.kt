/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.internal

import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemEngine
import at.released.weh.filesystem.dsl.FileSystemEngineConfig
import at.released.weh.filesystem.lock.GlobalLockFileSystemInterceptor
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor
import at.released.weh.host.EmbedderHostBuilder
import at.released.weh.host.FileSystemSimpleConfigBlock

internal fun <E : FileSystemEngineConfig> EmbedderHostBuilder.thisOrCreateDefaultFileSystem(
    engine: FileSystemEngine<E>,
    fsLoggerTag: String,
): FileSystem {
    val overridenFileSystem = this.fileSystem().fileSystem
    return if (overridenFileSystem != null) {
        // If the file system is overridden, we should not close it.
        NoCloseFileSystemDecorator(overridenFileSystem)
    } else {
        createDefaultFileSystem(engine, fsLoggerTag)
    }
}

private fun <E : FileSystemEngineConfig> EmbedderHostBuilder.createDefaultFileSystem(
    engine: FileSystemEngine<E>,
    fsLoggerTag: String,
): FileSystem {
    val builder: EmbedderHostBuilder = this
    val fileSystemConfig: FileSystemSimpleConfigBlock = builder.fileSystem()
    return FileSystem(engine) {
        addInterceptor(GlobalLockFileSystemInterceptor())
        addInterceptor(LoggingFileSystemInterceptor(builder.logger.withTag(fsLoggerTag)))
        stdio {
            stdinProvider = builder.stdin
            stdoutProvider = builder.stdout
            stderrProvider = builder.stderr
        }
        isRootAccessAllowed = fileSystemConfig.isRootAccessAllowed
        currentWorkingDirectory = fileSystemConfig.currentWorkingDirectory
        preopened {
            addAll(fileSystemConfig.preopenedDirectories)
        }
    }
}

private class NoCloseFileSystemDecorator(
    val delegate: FileSystem,
) : FileSystem by delegate {
    // Do not close delegate
    override fun close(): Unit = Unit
}
