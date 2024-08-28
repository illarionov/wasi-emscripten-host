/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.jvm

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.nio.NioFileSystem
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHost.Clock
import at.released.weh.host.EmbedderHost.CommandArgsProvider
import at.released.weh.host.EmbedderHost.EntropySource
import at.released.weh.host.EmbedderHost.LocalTimeFormatter
import at.released.weh.host.EmbedderHost.MonotonicClock
import at.released.weh.host.EmbedderHost.SystemEnvProvider
import at.released.weh.host.EmbedderHost.TimeZoneInfoProvider
import at.released.weh.host.ext.DefaultFileSystem

public class JvmEmbedderHost(
    public override val rootLogger: Logger = Logger,
    public override val systemEnvProvider: SystemEnvProvider = JvmSystemEnvProvider,
    public override val commandArgsProvider: CommandArgsProvider = JvmCommandArgsProvider,
    public override val fileSystem: FileSystem = DefaultFileSystem(NioFileSystem, rootLogger.withTag("FSnio")),
    public override val clock: Clock = JvmClock,
    public override val monotonicClock: MonotonicClock = JvmMonotonicClock,
    public override val localTimeFormatter: LocalTimeFormatter = JvmLocalTimeFormatter(),
    public override val timeZoneInfo: TimeZoneInfoProvider = JvmTimeZoneInfoProvider(),
    public override val entropySource: EntropySource = JvmEntropySource(),
) : EmbedderHost {
    internal object JvmSystemEnvProvider : SystemEnvProvider {
        override fun getSystemEnv(): Map<String, String> = System.getenv()
    }

    internal object JvmCommandArgsProvider : CommandArgsProvider {
        override fun getCommandArgs(): List<String> = emptyList()
    }

    internal object JvmClock : Clock {
        override fun getCurrentTimeEpochMilliseconds(): Long = System.currentTimeMillis()
    }

    internal object JvmMonotonicClock : MonotonicClock {
        override fun getTimeMarkNanoseconds(): Long = System.nanoTime()
    }
}
