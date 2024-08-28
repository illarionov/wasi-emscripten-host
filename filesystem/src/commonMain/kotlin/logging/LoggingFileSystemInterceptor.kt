/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.logging

import arrow.core.Either
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.FileSystemInterceptor.Chain
import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents.OperationEnd
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents.OperationStart
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.OperationLoggingLevel.BASIC
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.OperationLoggingLevel.NONE
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.OperationLoggingLevel.VERBOSE
import at.released.weh.filesystem.op.FileSystemOperation
import kotlin.time.Duration
import kotlin.time.measureTimedValue

public class LoggingFileSystemInterceptor(
    private val logger: (message: () -> String) -> Unit,
    private val logEvents: LoggingEvents = LoggingEvents(),
    private val operationLevels: Map<FileSystemOperation<*, *, *>, LoggingEvents> = emptyMap(),
) : FileSystemInterceptor {
    override fun <I : Any, E : FileSystemOperationError, R : Any> intercept(chain: Chain<I, E, R>): Either<E, R> {
        val loggingEvents = getLoggingEvents(chain.operation)
        logOperationStart(loggingEvents.start, chain.operation, chain.input)

        val duration: Duration?
        val output: Either<*, *>
        if (loggingEvents.end.trackDuration) {
            val timedValue = measureTimedValue {
                chain.proceed(chain.input)
            }
            output = timedValue.value
            duration = timedValue.duration
        } else {
            output = chain.proceed(chain.input)
            duration = null
        }

        logOperationEnd(loggingEvents.end, chain.operation, chain.input, output, duration)

        return output
    }

    private fun getLoggingEvents(operation: FileSystemOperation<*, *, *>): LoggingEvents {
        return operationLevels[operation] ?: logEvents
    }

    private fun logOperationStart(
        level: OperationStart,
        operation: FileSystemOperation<*, *, *>,
        input: Any,
    ) {
        if (level.inputs == NONE) {
            return
        }
        logger { buildOperationStartMessage(level, operation, input) }
    }

    private fun logOperationEnd(
        level: OperationEnd,
        operation: FileSystemOperation<*, *, *>,
        input: Any,
        output: Either<FileSystemOperationError, Any>,
        duration: Duration?,
    ) {
        if (level.inputs == NONE && level.outputs == NONE) {
            return
        }
        logger {
            buildOperationEndMessage(level, operation, input, output, duration)
        }
    }

    public enum class OperationLoggingLevel {
        NONE,
        NAME,
        BASIC,
        VERBOSE,
    }

    public data class LoggingEvents(
        val start: OperationStart = OperationStart(inputs = NONE),
        val end: OperationEnd = OperationEnd(),
    ) {
        public data class OperationStart(
            val inputs: OperationLoggingLevel = BASIC,
        )

        public data class OperationEnd(
            val inputs: OperationLoggingLevel = BASIC,
            val outputs: OperationLoggingLevel = BASIC,
            val trackDuration: Boolean = false,
        )
    }

    public companion object {
        private fun buildOperationStartMessage(
            level: OperationStart,
            operation: FileSystemOperation<*, *, *>,
            input: Any,
        ): String = buildString {
            append("^")
            append(operation.tag)

            if (level.inputs >= BASIC) {
                append("($input)")
            }
        }

        private fun buildOperationEndMessage(
            level: OperationEnd,
            operation: FileSystemOperation<*, *, *>,
            input: Any,
            output: Either<FileSystemOperationError, Any>,
            duration: Duration?,
        ): String {
            val status = buildString {
                append(operation.tag)

                append("(): ")
                output.fold(
                    ifLeft = {
                        append(it.errno)
                        if (level.outputs >= VERBOSE) {
                            append("(${it.message})")
                        }
                    },
                    ifRight = {
                        append("OK")
                    },
                )
                append(".")
            }
            val inputsDescription: String? = if (level.inputs >= BASIC) {
                "Inputs: $input."
            } else {
                null
            }
            val outputsDescription: String? = if (level.outputs >= BASIC && output.isRight()) {
                "Outputs: ${output.getOrNull()}"
            } else {
                null
            }
            val durationDescription = duration?.let {
                "Duration: $it"
            }

            return listOfNotNull(
                status,
                inputsDescription,
                outputsDescription,
                durationDescription,
            ).joinToString(" ")
        }
    }
}
