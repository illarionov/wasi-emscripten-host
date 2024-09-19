/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.filesystem.nio.NioFileSystem
import at.released.weh.host.EmbedderHost.Builder
import at.released.weh.host.internal.DefaultFileSystem
import at.released.weh.host.jvm.JvmEmbedderHost
import at.released.weh.host.jvm.JvmEmbedderHost.JvmClock
import at.released.weh.host.jvm.JvmEmbedderHost.JvmCommandArgsProvider
import at.released.weh.host.jvm.JvmEmbedderHost.JvmMonotonicClock
import at.released.weh.host.jvm.JvmEmbedderHost.JvmSystemEnvProvider
import at.released.weh.host.jvm.JvmEntropySource
import at.released.weh.host.jvm.JvmLocalTimeFormatter
import at.released.weh.host.jvm.JvmTimeZoneInfoProvider

internal actual fun createDefaultEmbedderHost(builder: Builder): EmbedderHost = JvmEmbedderHost(
    rootLogger = builder.rootLogger,
    systemEnvProvider = builder.systemEnvProvider ?: JvmSystemEnvProvider,
    commandArgsProvider = builder.commandArgsProvider ?: JvmCommandArgsProvider,
    fileSystem = builder.fileSystem ?: DefaultFileSystem(NioFileSystem, builder.rootLogger.withTag("FSnio")),
    clock = builder.clock ?: JvmClock,
    monotonicClock = builder.monotonicClock ?: JvmMonotonicClock,
    localTimeFormatter = builder.localTimeFormatter ?: JvmLocalTimeFormatter(),
    timeZoneInfo = builder.timeZoneInfo ?: JvmTimeZoneInfoProvider(),
    entropySource = builder.entropySource ?: JvmEntropySource(),
)
