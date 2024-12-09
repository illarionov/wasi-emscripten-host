/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.logging

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import at.released.weh.filesystem.FileSystemInterceptor.Chain
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents.OperationEnd
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.LoggingEvents.OperationStart
import at.released.weh.filesystem.logging.LoggingFileSystemInterceptor.OperationLoggingLevel.BASIC
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.test.fixtures.TestFileSystem
import at.released.weh.filesystem.test.fixtures.toVirtualPath
import kotlin.test.Test

class LoggingFileSystemDecoratorTest {
    @Test
    fun decorator_should_log_success_requests_on_basic_level() {
        val delegateFs = TestFileSystem()
        delegateFs.onOperation(ReadLink) {
            "/link".right()
        }

        val loggedMessages: MutableList<String> = mutableListOf()
        val loggingInterceptor = LoggingFileSystemInterceptor(
            logger = {
                loggedMessages += it()
            },
            logEvents = LoggingEvents(
                start = OperationStart(inputs = BASIC),
                end = OperationEnd(
                    inputs = BASIC,
                    outputs = BASIC,
                    trackDuration = false,
                ),
            ),
        )
        val chain = object : Chain<ReadLink, ReadLinkError, VirtualPath> {
            override val operation: FileSystemOperation<ReadLink, ReadLinkError, VirtualPath> = ReadLink
            override val input: ReadLink = ReadLink(
                path = "/testPath".toVirtualPath(),
                baseDirectory = CurrentWorkingDirectory,
            )

            override fun proceed(input: ReadLink): Either<ReadLinkError, VirtualPath> = "/link".toVirtualPath().right()
        }

        loggingInterceptor.intercept(chain)

        assertThat(loggedMessages).containsExactly(
            "^readlink(ReadLink(path=/testPath, baseDirectory=CurrentWorkingDirectory))",
            "readlink(): OK. Inputs: ReadLink(path=/testPath, baseDirectory=CurrentWorkingDirectory). Outputs: /link",
        )
    }
}
