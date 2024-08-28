/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.test.fixtures

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHost.Clock
import at.released.weh.host.EmbedderHost.CommandArgsProvider
import at.released.weh.host.EmbedderHost.EntropySource
import at.released.weh.host.EmbedderHost.LocalTimeFormatter
import at.released.weh.host.EmbedderHost.MonotonicClock
import at.released.weh.host.EmbedderHost.SystemEnvProvider
import at.released.weh.host.EmbedderHost.TimeZoneInfoProvider
import at.released.weh.host.include.StructTm
import at.released.weh.host.include.TimeZoneInfo
import at.released.weh.test.utils.KermitLogger

public open class TestEmbedderHost(
    override var rootLogger: Logger = KermitLogger(),
    override var systemEnvProvider: SystemEnvProvider = SystemEnvProvider { emptyMap() },
    override var commandArgsProvider: CommandArgsProvider = CommandArgsProvider { emptyList() },
    override var fileSystem: FileSystem = TestFileSystem(),
    override var monotonicClock: MonotonicClock = MonotonicClock { Long.MAX_VALUE },
    override var clock: Clock = Clock { Long.MAX_VALUE },
    override var localTimeFormatter: LocalTimeFormatter = LocalTimeFormatter {
        StructTm(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1)
    },
    override var timeZoneInfo: TimeZoneInfoProvider = TimeZoneInfoProvider {
        TimeZoneInfo(-1, -1, "Dummy", "Dummy")
    },
    override var entropySource: EntropySource = EntropySource { size ->
        ByteArray(size) { 4 }
    },
) : EmbedderHost
