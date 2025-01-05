/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host

import at.released.weh.common.api.Logger
import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.filesystem.stdio.StdioSink
import at.released.weh.filesystem.stdio.StdioSource
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.CputimeSource
import at.released.weh.host.clock.MonotonicClock
import kotlin.jvm.JvmSynthetic

internal expect fun createDefaultEmbedderHost(builder: EmbedderHostBuilder): EmbedderHost

@WasiEmscriptenHostDsl
public class EmbedderHostBuilder {
    /**
     * The logger for the embedder host.
     * Can be overridden to log internal debug messages.
     */
    @set:JvmSynthetic // Hide from Java
    public var rootLogger: Logger = Logger

    /**
     * Implementation of the STDIN stream.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var stdinProvider: StdioSource.Provider? = null

    /**
     * Implementation of the STDOUT stream.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var stdoutProvider: StdioSink.Provider? = null

    /**
     * Implementation of the STDERR stream.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var stderrProvider: StdioSink.Provider? = null

    /**
     * Provider of the system environment variables.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var systemEnvProvider: SystemEnvProvider? = null

    /**
     * Provider of the application's command line arguments.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var commandArgsProvider: CommandArgsProvider? = null

    /**
     * Implementation of the real-time clock.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var clock: Clock? = null

    /**
     * Implementation of the monotonic clock.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var monotonicClock: MonotonicClock? = null

    /**
     * Implementation of clocks used to measure CPU time.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var cputimeSource: CputimeSource? = null

    /**
     * Source of the entropy.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var entropySource: EntropySource? = null

    /**
     * Implementation of the local time formatter.
     * Used in Emscripten bindings.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var localTimeFormatter: LocalTimeFormatter? = null

    /**
     * Implementation of time zone information provider.
     * Used in Emscripten bindings.
     * Set to null to use default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var timeZoneInfoProvider: TimeZoneInfo.Provider? = null

    private var fileSystemConfigBlock: FileSystemSimpleConfigBlock = FileSystemSimpleConfigBlock()

    /**
     * The logger for the embedder host.
     * Can be overridden to log internal debug messages.
     */
    public fun setRootLogger(rootLogger: Logger): EmbedderHostBuilder = apply {
        this.rootLogger = rootLogger
    }

    /**
     * Implementation of the STDIN stream.
     * Set to null to use the default implementation.
     */
    public fun setStdinProvider(stdinProvider: StdioSource.Provider?): EmbedderHostBuilder = apply {
        this.stdinProvider = stdinProvider
    }

    /**
     * Implementation of the STDOUT stream.
     * Set to null to use the default implementation.
     */
    public fun setStdoutProvider(stdoutProvider: StdioSink.Provider?): EmbedderHostBuilder = apply {
        this.stdoutProvider = stdoutProvider
    }

    /**
     * Implementation of the STDERR stream.
     * Set to null to use the default implementation.
     */
    public fun setStderrProvider(stderrProvider: StdioSink.Provider?): EmbedderHostBuilder = apply {
        this.stderrProvider = stderrProvider
    }

    /**
     * Provider of the system environment variables.
     * Set to null to use the default implementation.
     */
    public fun setSystemEnvProvider(systemEnvProvider: SystemEnvProvider?): EmbedderHostBuilder = apply {
        this.systemEnvProvider = systemEnvProvider
    }

    /**
     * Provider of the application's command line arguments.
     * Set to null to use the default implementation.
     */
    public fun setCommandArgsProvider(commandArgsProvider: CommandArgsProvider?): EmbedderHostBuilder = apply {
        this.commandArgsProvider = commandArgsProvider
    }

    /**
     * Implementation of the real-time clock.
     * Set to null to use the default implementation.
     */
    public fun setClock(clock: Clock?): EmbedderHostBuilder = apply {
        this.clock = clock
    }

    /**
     * Implementation of the monotonic clock.
     * Set to null to use the default implementation.
     */
    public fun setMonotonicClock(monotonicClock: MonotonicClock?): EmbedderHostBuilder = apply {
        this.monotonicClock = monotonicClock
    }

    /**
     * Implementation of clocks used to measure CPU time.
     * Set to null to use the default implementation.
     */
    public fun setCputimeSource(cputimeSource: CputimeSource?): EmbedderHostBuilder = apply {
        this.cputimeSource = cputimeSource
    }

    /**
     * Source of the entropy.
     * Set to null to use the default implementation.
     */
    public fun setEntropySource(entropySource: EntropySource?): EmbedderHostBuilder = apply {
        this.entropySource = entropySource
    }

    /**
     * Implementation of the local time formatter.
     * Used in implementation of Emscripten bindings.
     * Set to null to use the default implementation.
     */
    public fun setLocalTimeFormatter(localTimeFormatter: LocalTimeFormatter?): EmbedderHostBuilder = apply {
        this.localTimeFormatter = localTimeFormatter
    }

    /**
     * Implementation of time zone information provider.
     * Used in Emscripten bindings.
     * Set to null to use default implementation.
     */
    public fun setTimeZoneInfoProvider(timeZoneInfo: TimeZoneInfo.Provider?): EmbedderHostBuilder = apply {
        this.timeZoneInfoProvider = timeZoneInfo
    }

    /**
     * Sets file system parameters.
     */
    public fun fileSystem(): FileSystemSimpleConfigBlock = fileSystemConfigBlock

    /**
     * Sets file system parameters.
     */
    @JvmSynthetic // Hide from Java
    public fun fileSystem(block: FileSystemSimpleConfigBlock.() -> Unit): EmbedderHostBuilder = apply {
        block(fileSystemConfigBlock)
    }

    public fun build(): EmbedderHost = createDefaultEmbedderHost(this)

    public companion object {
        @JvmSynthetic // Hide from Java
        public operator fun invoke(
            block: EmbedderHostBuilder.() -> Unit = {},
        ): EmbedderHostBuilder {
            return EmbedderHostBuilder().apply(block)
        }
    }
}
