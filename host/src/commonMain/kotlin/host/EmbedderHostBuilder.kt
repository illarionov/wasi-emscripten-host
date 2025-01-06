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
    public var logger: Logger = Logger

    /**
     * Implementation of the STDIN stream.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var stdin: StdioSource.Provider? = null

    /**
     * Implementation of the STDOUT stream.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var stdout: StdioSink.Provider? = null

    /**
     * Implementation of the STDERR stream.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var stderr: StdioSink.Provider? = null

    /**
     * Provider of the system environment variables.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var systemEnv: SystemEnvProvider? = null

    /**
     * Provider of the application's command line arguments.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var commandArgs: CommandArgsProvider? = null

    /**
     * Implementation of the real-time clock.
     * Set to null to use the default implementation.
     */
    @set:JvmSynthetic // Hide from Java
    public var realTimeClock: Clock? = null

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
    public var cpuTime: CputimeSource? = null

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
    public var timeZoneInfo: TimeZoneInfo.Provider? = null
    private var fileSystemConfigBlock: FileSystemSimpleConfigBlock = FileSystemSimpleConfigBlock()

    /**
     * The logger for the embedder host.
     * Can be overridden to log internal debug messages.
     */
    public fun setLogger(logger: Logger): EmbedderHostBuilder = apply {
        this.logger = logger
    }

    /**
     * Implementation of the STDIN stream.
     * Set to null to use the default implementation.
     */
    public fun setStdin(stdin: StdioSource.Provider?): EmbedderHostBuilder = apply {
        this.stdin = stdin
    }

    /**
     * Implementation of the STDOUT stream.
     * Set to null to use the default implementation.
     */
    public fun setStdout(stdout: StdioSink.Provider?): EmbedderHostBuilder = apply {
        this.stdout = stdout
    }

    /**
     * Implementation of the STDERR stream.
     * Set to null to use the default implementation.
     */
    public fun setStderr(stderr: StdioSink.Provider?): EmbedderHostBuilder = apply {
        this.stderr = stderr
    }

    /**
     * Provider of the system environment variables.
     * Set to null to use the default implementation.
     */
    public fun setSystemEnv(systemEnv: SystemEnvProvider?): EmbedderHostBuilder = apply {
        this.systemEnv = systemEnv
    }

    /**
     * Provider of the application's command line arguments.
     * Set to null to use the default implementation.
     */
    public fun setCommandArgs(commandArgs: CommandArgsProvider?): EmbedderHostBuilder = apply {
        this.commandArgs = commandArgs
    }

    /**
     * Implementation of the real-time clock.
     * Set to null to use the default implementation.
     */
    public fun setRealTimeClock(realTimeClock: Clock?): EmbedderHostBuilder = apply {
        this.realTimeClock = realTimeClock
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
    public fun setCpuTime(cpuTime: CputimeSource?): EmbedderHostBuilder = apply {
        this.cpuTime = cpuTime
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
    public fun setTimeZoneInfo(timeZoneInfo: TimeZoneInfo.Provider?): EmbedderHostBuilder = apply {
        this.timeZoneInfo = timeZoneInfo
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
}
