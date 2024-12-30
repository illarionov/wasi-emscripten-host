/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import arrow.core.getOrElse
import at.released.weh.common.api.Logger
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.dsl.DirectoryConfigBlock
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.stdio.StdioSink
import at.released.weh.filesystem.stdio.StdioSource
import at.released.weh.host.EmbedderHost.Builder
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.MonotonicClock
import kotlin.jvm.JvmSynthetic

internal expect fun createDefaultEmbedderHost(builder: Builder): EmbedderHost

public interface EmbedderHost : AutoCloseable {
    public val rootLogger: Logger
    public val systemEnvProvider: SystemEnvProvider
    public val commandArgsProvider: CommandArgsProvider
    public val fileSystem: FileSystem
    public val monotonicClock: MonotonicClock
    public val clock: Clock
    public val cputimeSource: CputimeSource
    public val localTimeFormatter: LocalTimeFormatter
    public val timeZoneInfo: TimeZoneInfo.Provider
    public val entropySource: EntropySource

    @WasiEmscriptenHostDsl
    public class Builder {
        public var rootLogger: Logger = Logger
        public var stdinProvider: StdioSource.Provider? = null
        public var stdoutProvider: StdioSink.Provider? = null
        public var stderrProvider: StdioSink.Provider? = null
        public var systemEnvProvider: SystemEnvProvider? = null
        public var commandArgsProvider: CommandArgsProvider? = null
        public var fileSystem: FileSystem? = null
        public var clock: Clock? = null
        public var monotonicClock: MonotonicClock? = null
        public var cputimeSource: CputimeSource? = null
        public var localTimeFormatter: LocalTimeFormatter? = null
        public var timeZoneInfo: TimeZoneInfo.Provider? = null
        public var entropySource: EntropySource? = null

        @JvmSynthetic // Hide from Java
        internal val directoriesConfigBlock: DirectoryConfigBlock = DirectoryConfigBlock()

        public fun directories(): DirectoriesBuilder = DirectoriesBuilder()

        public fun build(): EmbedderHost = createDefaultEmbedderHost(this)

        @WasiEmscriptenHostDsl
        public inner class DirectoriesBuilder internal constructor() {
            public fun setCurrentWorkingDirectory(directory: String): DirectoriesBuilder = apply {
                this@Builder.directoriesConfigBlock.currentWorkingDirectory = directory
            }

            public fun setAllowRootAccess(allowRootAccess: Boolean): DirectoriesBuilder = apply {
                this@Builder.directoriesConfigBlock.isRootAccessAllowed = allowRootAccess
            }

            public fun addPreopenedDirectory(
                realPath: String,
                virtualPath: String,
            ): DirectoriesBuilder {
                val virtPath = VirtualPath.create(virtualPath).getOrElse {
                    error("Invalid virtual path. The path `$virtualPath` must be a Unix-like path")
                }
                this@Builder.directoriesConfigBlock.preopened {
                    add(PreopenedDirectory(realPath, virtPath))
                }
                return this
            }

            public fun done(): Builder = this@Builder
        }
    }
}
