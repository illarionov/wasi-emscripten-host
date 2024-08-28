/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux

import at.released.weh.common.api.Logger
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.LinuxFileSystem
import at.released.weh.host.EmbedderHost
import at.released.weh.host.EmbedderHost.Clock
import at.released.weh.host.EmbedderHost.CommandArgsProvider
import at.released.weh.host.EmbedderHost.EntropySource
import at.released.weh.host.EmbedderHost.LocalTimeFormatter
import at.released.weh.host.EmbedderHost.MonotonicClock
import at.released.weh.host.EmbedderHost.SystemEnvProvider
import at.released.weh.host.EmbedderHost.TimeZoneInfoProvider
import at.released.weh.host.ext.DefaultFileSystem
import at.released.weh.host.internal.CommonClock
import at.released.weh.host.internal.CommonMonotonicClock
import at.released.weh.host.internal.EmptyCommandArgsProvider

public class LinuxEmbedderHost(
    override val rootLogger: Logger,
    override val systemEnvProvider: SystemEnvProvider = LinuxSystemEnvProvider,
    override val commandArgsProvider: CommandArgsProvider = EmptyCommandArgsProvider,
    override val fileSystem: FileSystem = DefaultFileSystem(LinuxFileSystem, rootLogger.withTag("FSlnx")),
    override val monotonicClock: MonotonicClock = CommonMonotonicClock(),
    override val clock: Clock = CommonClock(),
    override val localTimeFormatter: LocalTimeFormatter = LinuxLocalTimeFormatter,
    override val timeZoneInfo: TimeZoneInfoProvider = LinuxTimeZoneInfoProvider,
    override val entropySource: EntropySource = LinuxEntropySource,
) : EmbedderHost
