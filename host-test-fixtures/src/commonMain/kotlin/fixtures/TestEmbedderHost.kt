/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.test.fixtures

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.CommandArgsProvider
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EntropySource
import at.released.weh.host.LocalTimeFormatter
import at.released.weh.host.LocalTimeFormatter.StructTm
import at.released.weh.host.SystemEnvProvider
import at.released.weh.host.TimeZoneInfo
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.test.logger.TestLogger
import kotlin.time.Duration.Companion.milliseconds

public open class TestEmbedderHost(
    override var rootLogger: Logger = TestLogger(),
    override var systemEnvProvider: SystemEnvProvider = SystemEnvProvider { emptyMap() },
    override var commandArgsProvider: CommandArgsProvider = CommandArgsProvider { emptyList() },
    override var fileSystem: FileSystem = TestFileSystem(),
    override var monotonicClock: MonotonicClock = object : MonotonicClock {
        override fun getTimeMarkNanoseconds(): Long = Long.MAX_VALUE
        override fun getResolutionNanoseconds(): Long = 1.milliseconds.inWholeNanoseconds
    },
    override var clock: Clock = object : Clock {
        override fun getCurrentTimeEpochNanoseconds(): Long = Long.MAX_VALUE
        override fun getResolutionNanoseconds(): Long = 1.milliseconds.inWholeNanoseconds
    },
    override var cputimeSource: CputimeSource = TestCputimeSource(),
    override var localTimeFormatter: LocalTimeFormatter = LocalTimeFormatter {
        StructTm(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1)
    },
    override var timeZoneInfo: TimeZoneInfo.Provider = TimeZoneInfo.Provider {
        TimeZoneInfo(-1, -1, "Dummy", "Dummy")
    },
    override var entropySource: EntropySource = EntropySource { size ->
        ByteArray(size) { 4 }
    },
) : EmbedderHost
