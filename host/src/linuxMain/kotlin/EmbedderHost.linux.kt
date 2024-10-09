/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.filesystem.LinuxFileSystem
import at.released.weh.host.EmbedderHost.Builder
import at.released.weh.host.internal.CommonClock
import at.released.weh.host.internal.CommonMonotonicClock
import at.released.weh.host.internal.DefaultFileSystem
import at.released.weh.host.internal.EmptyCommandArgsProvider
import at.released.weh.host.linux.LinuxEntropySource
import at.released.weh.host.linux.LinuxLocalTimeFormatter
import at.released.weh.host.linux.LinuxSystemEnvProvider
import at.released.weh.host.linux.LinuxTimeZoneInfoProvider

internal actual fun createDefaultEmbedderHost(builder: Builder): EmbedderHost = object : EmbedderHost {
    override val rootLogger = builder.rootLogger
    override val systemEnvProvider = builder.systemEnvProvider ?: LinuxSystemEnvProvider
    override val commandArgsProvider = builder.commandArgsProvider ?: EmptyCommandArgsProvider
    override val fileSystem = builder.fileSystem ?: DefaultFileSystem(
        LinuxFileSystem,
        builder.stdinProvider,
        builder.stdoutProvider,
        builder.stderrProvider,
        builder.directoriesConfigBlock,
        builder.rootLogger.withTag("FSlnx"),
    )
    override val monotonicClock = builder.monotonicClock ?: CommonMonotonicClock()
    override val clock = builder.clock ?: CommonClock()
    override val localTimeFormatter = builder.localTimeFormatter ?: LinuxLocalTimeFormatter
    override val timeZoneInfo = builder.timeZoneInfo ?: LinuxTimeZoneInfoProvider
    override val entropySource = builder.entropySource ?: LinuxEntropySource
}
