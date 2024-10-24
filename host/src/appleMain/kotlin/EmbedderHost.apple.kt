/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.filesystem.NotImplementedFileSystem
import at.released.weh.host.apple.AppleEmbedderHost
import at.released.weh.host.apple.AppleEmbedderHost.AppleSystemEnvProvider
import at.released.weh.host.apple.AppleEntropySource
import at.released.weh.host.apple.AppleLocalTimeFormatter
import at.released.weh.host.apple.AppleTimeZoneInfoProvider
import at.released.weh.host.apple.clock.AppleClock
import at.released.weh.host.apple.clock.AppleCputimeSource
import at.released.weh.host.apple.clock.AppleMonotonicClock
import at.released.weh.host.internal.EmptyCommandArgsProvider

internal actual fun createDefaultEmbedderHost(builder: EmbedderHost.Builder): EmbedderHost = AppleEmbedderHost(
    rootLogger = builder.rootLogger,
    systemEnvProvider = builder.systemEnvProvider ?: AppleSystemEnvProvider,
    commandArgsProvider = builder.commandArgsProvider ?: EmptyCommandArgsProvider,
    fileSystem = builder.fileSystem ?: NotImplementedFileSystem,
    monotonicClock = builder.monotonicClock ?: AppleMonotonicClock,
    clock = builder.clock ?: AppleClock,
    cputimeSource = builder.cputimeSource ?: AppleCputimeSource,
    localTimeFormatter = builder.localTimeFormatter ?: AppleLocalTimeFormatter(),
    timeZoneInfo = builder.timeZoneInfo ?: AppleTimeZoneInfoProvider(),
    entropySource = builder.entropySource ?: AppleEntropySource,
)
