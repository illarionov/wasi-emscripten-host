/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.apple

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.NotImplementedFileSystem
import at.released.weh.host.Clock
import at.released.weh.host.CommandArgsProvider
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EntropySource
import at.released.weh.host.LocalTimeFormatter
import at.released.weh.host.MonotonicClock
import at.released.weh.host.SystemEnvProvider
import at.released.weh.host.TimeZoneInfo
import at.released.weh.host.internal.CommonClock
import at.released.weh.host.internal.CommonMonotonicClock
import at.released.weh.host.internal.EmptyCommandArgsProvider

public class AppleEmbedderHost(
    override val rootLogger: Logger,
    override val systemEnvProvider: SystemEnvProvider = AppleSystemEnvProvider,
    override val commandArgsProvider: CommandArgsProvider = EmptyCommandArgsProvider,
    override val fileSystem: FileSystem = NotImplementedFileSystem,
    override val monotonicClock: MonotonicClock = CommonMonotonicClock(),
    override val clock: Clock = CommonClock(),
    override val localTimeFormatter: LocalTimeFormatter = AppleLocalTimeFormatter(),
    override val timeZoneInfo: TimeZoneInfo.Provider = AppleTimeZoneInfoProvider(),
    override val entropySource: EntropySource = AppleEntropySource,
) : EmbedderHost {
    internal object AppleSystemEnvProvider : SystemEnvProvider {
        override fun getSystemEnv(): Map<String, String> = emptyMap()
    }
}
