/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.stdio.SinkProvider
import at.released.weh.filesystem.stdio.SourceProvider
import at.released.weh.host.EmbedderHost.Builder

internal expect fun createDefaultEmbedderHost(builder: Builder): EmbedderHost

public interface EmbedderHost {
    public val rootLogger: Logger
    public val systemEnvProvider: SystemEnvProvider
    public val commandArgsProvider: CommandArgsProvider
    public val fileSystem: FileSystem
    public val monotonicClock: MonotonicClock
    public val clock: Clock
    public val localTimeFormatter: LocalTimeFormatter
    public val timeZoneInfo: TimeZoneInfo.Provider
    public val entropySource: EntropySource

    public class Builder {
        public var rootLogger: Logger = Logger
        public var stdinProvider: SourceProvider? = null
        public var stdoutProvider: SinkProvider? = null
        public var stderrProvider: SinkProvider? = null
        public var systemEnvProvider: SystemEnvProvider? = null
        public var commandArgsProvider: CommandArgsProvider? = null
        public var fileSystem: FileSystem? = null
        public var clock: Clock? = null
        public var monotonicClock: MonotonicClock? = null
        public var localTimeFormatter: LocalTimeFormatter? = null
        public var timeZoneInfo: TimeZoneInfo.Provider? = null
        public var entropySource: EntropySource? = null

        public fun build(): EmbedderHost = createDefaultEmbedderHost(this)
    }
}
