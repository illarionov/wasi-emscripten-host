/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.filesystem.LinuxFileSystem
import at.released.weh.host.EmbedderHost.Builder
import at.released.weh.host.ext.DefaultFileSystem
import at.released.weh.host.internal.CommonClock
import at.released.weh.host.internal.CommonMonotonicClock
import at.released.weh.host.internal.EmptyCommandArgsProvider
import at.released.weh.host.linux.LinuxEmbedderHost
import at.released.weh.host.linux.LinuxEntropySource
import at.released.weh.host.linux.LinuxLocalTimeFormatter
import at.released.weh.host.linux.LinuxSystemEnvProvider
import at.released.weh.host.linux.LinuxTimeZoneInfoProvider

internal actual fun createDefaultEmbedderHost(builder: Builder): EmbedderHost = LinuxEmbedderHost(
    rootLogger = builder.rootLogger,
    systemEnvProvider = builder.systemEnvProvider ?: LinuxSystemEnvProvider,
    commandArgsProvider = builder.commandArgsProvider ?: EmptyCommandArgsProvider,
    fileSystem = builder.fileSystem ?: DefaultFileSystem(LinuxFileSystem, builder.rootLogger.withTag("FSlnx")),
    monotonicClock = builder.monotonicClock ?: CommonMonotonicClock(),
    clock = builder.clock ?: CommonClock(),
    localTimeFormatter = builder.localTimeFormatter ?: LinuxLocalTimeFormatter,
    timeZoneInfo = builder.timeZoneInfo ?: LinuxTimeZoneInfoProvider,
    entropySource = builder.entropySource ?: LinuxEntropySource,
)
