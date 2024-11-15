/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.NotImplementedFileSystem
import at.released.weh.host.EmbedderHost.Builder
import at.released.weh.host.TimeZoneInfo.Provider
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.internal.EmptyCommandArgsProvider
import at.released.weh.host.windows.WindowsLocalTimeFormatter
import at.released.weh.host.windows.WindowsSystemEnvProvider
import at.released.weh.host.windows.clock.WindowsClock
import at.released.weh.host.windows.clock.WindowsCputimeSource
import at.released.weh.host.windows.clock.WindowsMonotonicClock

internal actual fun createDefaultEmbedderHost(builder: Builder): EmbedderHost = object : EmbedderHost {
    override val rootLogger: Logger = builder.rootLogger
    override val systemEnvProvider: SystemEnvProvider = builder.systemEnvProvider ?: WindowsSystemEnvProvider
    override val commandArgsProvider: CommandArgsProvider = builder.commandArgsProvider ?: EmptyCommandArgsProvider
    override val fileSystem: FileSystem = builder.fileSystem ?: NotImplementedFileSystem
    override val monotonicClock: MonotonicClock = builder.monotonicClock ?: WindowsMonotonicClock
    override val clock: Clock = builder.clock ?: WindowsClock
    override val cputimeSource: CputimeSource = builder.cputimeSource ?: WindowsCputimeSource
    override val localTimeFormatter: LocalTimeFormatter = builder.localTimeFormatter ?: WindowsLocalTimeFormatter
    override val timeZoneInfo: Provider = builder.timeZoneInfo ?: TODO()
    override val entropySource: EntropySource = builder.entropySource ?: TODO()
}

internal object EmptyEnvProvider : SystemEnvProvider {
    override fun getSystemEnv(): Map<String, String> = emptyMap()
}
