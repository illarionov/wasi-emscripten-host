/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.WindowsFileSystem
import at.released.weh.host.TimeZoneInfo.Provider
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.internal.EmptyCommandArgsProvider
import at.released.weh.host.internal.thisOrCreateDefaultFileSystem
import at.released.weh.host.windows.WindowsEntropySource
import at.released.weh.host.windows.WindowsLocalTimeFormatter
import at.released.weh.host.windows.WindowsSystemEnvProvider
import at.released.weh.host.windows.WindowsTimeZoneInfoProvider
import at.released.weh.host.windows.clock.WindowsClock
import at.released.weh.host.windows.clock.WindowsCputimeSource
import at.released.weh.host.windows.clock.WindowsMonotonicClock

internal actual fun createDefaultEmbedderHost(builder: EmbedderHostBuilder): EmbedderHost = object : EmbedderHost {
    override val rootLogger: Logger = builder.logger
    override val systemEnvProvider: SystemEnvProvider = builder.systemEnv ?: WindowsSystemEnvProvider
    override val commandArgsProvider: CommandArgsProvider = builder.commandArgs ?: EmptyCommandArgsProvider
    override val fileSystem = builder.thisOrCreateDefaultFileSystem(WindowsFileSystem, "FSmingw")
    override val monotonicClock: MonotonicClock = builder.monotonicClock ?: WindowsMonotonicClock
    override val clock: Clock = builder.realTimeClock ?: WindowsClock
    override val cputimeSource: CputimeSource = builder.cpuTime ?: WindowsCputimeSource
    override val localTimeFormatter: LocalTimeFormatter = builder.localTimeFormatter ?: WindowsLocalTimeFormatter
    override val timeZoneInfoProvider: Provider = builder.timeZoneInfo ?: WindowsTimeZoneInfoProvider
    override val entropySource: EntropySource = builder.entropySource ?: WindowsEntropySource
    override fun close() {
        fileSystem.close()
    }
}
