/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.filesystem.nio.NioFileSystem
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.UnsupportedCputimeSource
import at.released.weh.host.internal.thisOrCreateDefaultFileSystem
import at.released.weh.host.jvm.JvmCommandArgsProvider
import at.released.weh.host.jvm.JvmEntropySource
import at.released.weh.host.jvm.JvmLocalTimeFormatter
import at.released.weh.host.jvm.JvmSystemEnvProvider
import at.released.weh.host.jvm.JvmTimeZoneInfoProvider
import at.released.weh.host.jvm.clock.JvmClock
import at.released.weh.host.jvm.clock.JvmMonotonicClock

@JvmSynthetic // Hide from Java
internal actual fun createDefaultEmbedderHost(builder: EmbedderHostBuilder): EmbedderHost = object : EmbedderHost {
    override val rootLogger = builder.logger
    override val systemEnvProvider = builder.systemEnv ?: JvmSystemEnvProvider
    override val commandArgsProvider = builder.commandArgs ?: JvmCommandArgsProvider
    override val fileSystem = builder.thisOrCreateDefaultFileSystem(NioFileSystem, "FSnio")
    override val clock = builder.realTimeClock ?: JvmClock
    override val cputimeSource: CputimeSource = builder.cpuTime ?: UnsupportedCputimeSource
    override val monotonicClock = builder.monotonicClock ?: JvmMonotonicClock
    override val localTimeFormatter = builder.localTimeFormatter ?: JvmLocalTimeFormatter()
    override val timeZoneInfoProvider = builder.timeZoneInfo ?: JvmTimeZoneInfoProvider()
    override val entropySource = builder.entropySource ?: JvmEntropySource()
    override fun close() {
        fileSystem.close()
    }
}
