/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.filesystem.nio.NioFileSystem
import at.released.weh.host.EmbedderHost.Builder
import at.released.weh.host.internal.DefaultFileSystem
import at.released.weh.host.jvm.JvmClock
import at.released.weh.host.jvm.JvmCommandArgsProvider
import at.released.weh.host.jvm.JvmEntropySource
import at.released.weh.host.jvm.JvmLocalTimeFormatter
import at.released.weh.host.jvm.JvmMonotonicClock
import at.released.weh.host.jvm.JvmSystemEnvProvider
import at.released.weh.host.jvm.JvmTimeZoneInfoProvider

internal actual fun createDefaultEmbedderHost(builder: Builder): EmbedderHost = object : EmbedderHost {
    override val rootLogger = builder.rootLogger
    override val systemEnvProvider = builder.systemEnvProvider ?: JvmSystemEnvProvider
    override val commandArgsProvider = builder.commandArgsProvider ?: JvmCommandArgsProvider
    override val fileSystem = builder.fileSystem ?: DefaultFileSystem(
        NioFileSystem,
        builder.stdinProvider,
        builder.stdoutProvider,
        builder.stderrProvider,
        builder.directoriesConfigBlock,
        builder.rootLogger.withTag("FSnio"),
    )
    override val clock = builder.clock ?: JvmClock
    override val monotonicClock = builder.monotonicClock ?: JvmMonotonicClock
    override val localTimeFormatter = builder.localTimeFormatter ?: JvmLocalTimeFormatter()
    override val timeZoneInfo = builder.timeZoneInfo ?: JvmTimeZoneInfoProvider()
    override val entropySource = builder.entropySource ?: JvmEntropySource()
}
