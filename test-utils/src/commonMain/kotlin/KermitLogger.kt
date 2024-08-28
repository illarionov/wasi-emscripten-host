/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("IDENTIFIER_LENGTH")

package at.released.weh.test.utils

import at.released.weh.common.api.Logger
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag
import co.touchlab.kermit.Logger as KermitLogger

internal expect val currentTimestamp: ULong
internal expect val currentThreadId: ULong

public open class KermitLogger(
    tag: String = "WSOH",
    private val minSeverity: Severity = Severity.Verbose,
) : Logger {
    private val delegate: KermitLogger = KermitLogger.apply {
        setMinSeverity(minSeverity)
        setLogWriters(TestWriter(MessageFormatter))
    }.withTag(tag)

    @Suppress("MagicNumber")
    internal object MessageFormatter : MessageStringFormatter {
        override fun formatSeverity(severity: Severity): String = listOf(
            currentTimestamp.toString().padEnd(13),
            currentThreadId.toString().padEnd(4),
            severity.toString().take(1),
        ).joinToString(" ")

        override fun formatTag(tag: Tag): String = "${tag.tag}:"
    }

    override fun withTag(tag: String): Logger = KermitLogger(tag, minSeverity)

    override fun v(throwable: Throwable?, message: () -> String) {
        delegate.v(message(), throwable)
    }

    override fun d(throwable: Throwable?, message: () -> String) {
        delegate.d(message(), throwable)
    }

    override fun i(throwable: Throwable?, message: () -> String) {
        delegate.i(message(), throwable)
    }

    override fun w(throwable: Throwable?, message: () -> String) {
        delegate.w(message(), throwable)
    }

    override fun e(throwable: Throwable?, message: () -> String) {
        delegate.e(message(), throwable)
    }

    override fun a(throwable: Throwable?, message: () -> String) {
        delegate.a(message(), throwable)
    }

    @Suppress("DEBUG_PRINT")
    internal class TestWriter(
        private val messageStringFormatter: MessageStringFormatter = MessageFormatter,
    ) : LogWriter() {
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            println(messageStringFormatter.formatMessage(severity, Tag(tag), Message(message)))
            throwable?.let {
                println(it.stackTraceToString())
            }
        }
    }
}
