/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.MonotonicClock
import kotlin.jvm.JvmSynthetic

@JvmSynthetic // Hide from Java
public fun EmbedderHost(
    block: EmbedderHostBuilder.() -> Unit = {},
): EmbedderHost {
    return EmbedderHostBuilder().apply(block).build()
}

public interface EmbedderHost : AutoCloseable {
    public val rootLogger: Logger
    public val systemEnvProvider: SystemEnvProvider
    public val commandArgsProvider: CommandArgsProvider
    public val fileSystem: FileSystem
    public val monotonicClock: MonotonicClock
    public val clock: Clock
    public val cputimeSource: CputimeSource
    public val localTimeFormatter: LocalTimeFormatter
    public val timeZoneInfoProvider: TimeZoneInfo.Provider
    public val entropySource: EntropySource
}
