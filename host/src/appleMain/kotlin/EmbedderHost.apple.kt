/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.AppleFileSystem
import at.released.weh.host.TimeZoneInfo.Provider
import at.released.weh.host.apple.AppleEntropySource
import at.released.weh.host.apple.AppleLocalTimeFormatter
import at.released.weh.host.apple.AppleTimeZoneInfoProvider
import at.released.weh.host.apple.clock.AppleClock
import at.released.weh.host.apple.clock.AppleCputimeSource
import at.released.weh.host.apple.clock.AppleMonotonicClock
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.internal.EmptyCommandArgsProvider
import at.released.weh.host.internal.thisOrCreateDefaultFileSystem

internal expect val appleSystemEnvProvider: SystemEnvProvider

internal actual fun createDefaultEmbedderHost(builder: EmbedderHost.Builder): EmbedderHost = object : EmbedderHost {
    override val rootLogger: Logger = builder.rootLogger
    override val systemEnvProvider: SystemEnvProvider = builder.systemEnvProvider ?: appleSystemEnvProvider
    override val commandArgsProvider: CommandArgsProvider = builder.commandArgsProvider ?: EmptyCommandArgsProvider
    override val fileSystem = builder.thisOrCreateDefaultFileSystem(AppleFileSystem, "FSappl")
    override val monotonicClock: MonotonicClock = builder.monotonicClock ?: AppleMonotonicClock
    override val clock: Clock = builder.clock ?: AppleClock
    override val cputimeSource: CputimeSource = builder.cputimeSource ?: AppleCputimeSource
    override val localTimeFormatter: LocalTimeFormatter = builder.localTimeFormatter ?: AppleLocalTimeFormatter
    override val timeZoneInfo: Provider = builder.timeZoneInfo ?: AppleTimeZoneInfoProvider
    override val entropySource: EntropySource = builder.entropySource ?: AppleEntropySource
    override fun close() {
        fileSystem.close()
    }
}

internal object EmptyEnvProvider : SystemEnvProvider {
    override fun getSystemEnv(): Map<String, String> = emptyMap()
}
