/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.fdresource

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.NotSupported
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_STDERR_FD
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_STDIN_FD
import at.released.weh.filesystem.internal.FileDescriptorTable.Companion.WASI_STDOUT_FD
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.fdresource.StdioFileFdResource
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.filesystem.posix.stdio.StdioWithPollableFileDescriptor
import at.released.weh.filesystem.stdio.ExhaustedRawSource
import at.released.weh.filesystem.stdio.StandardInputOutput
import at.released.weh.filesystem.stdio.StdioSink
import at.released.weh.filesystem.stdio.StdioSource

internal class NativeStdioFileFdResource(
    val sourceProvider: StdioSource.Provider,
    val sinkProvider: StdioSink.Provider,
    private val stdioResource: StdioFileFdResource = StdioFileFdResource(sourceProvider, sinkProvider),
) : ResourceWithPollableFileDescriptor, FdResource by stdioResource {
    override fun getPollableFileDescriptor(event: FileDescriptorEventType): Either<NotSupported, Int> {
        return when (event) {
            FileDescriptorEventType.READ -> stdioResource.getOrOpenSource()
            FileDescriptorEventType.WRITE -> stdioResource.getOrOpenSink()
        }.fold(
            ifLeft = { (-1).right() },
            ifRight = { source ->
                if (source is StdioWithPollableFileDescriptor) {
                    source.pollableFileDescriptor.right()
                } else {
                    NOT_SUPPORTED_ERROR
                }
            },
        )
    }

    public companion object {
        private val NOT_SUPPORTED_ERROR = NotSupported("Resource is not pollable").left()
        fun StandardInputOutput.toFileDescriptorMapWithNativeFd(): Map<FileDescriptor, FdResource> {
            val stdInStdOut = NativeStdioFileFdResource(
                sourceProvider = this.stdinProvider,
                sinkProvider = this.stdoutProvider,
            )
            val stdErr = NativeStdioFileFdResource(
                sourceProvider = StdioSource.Provider(::ExhaustedRawSource),
                sinkProvider = this.stderrProvider,
            )
            return mapOf(
                WASI_STDIN_FD to stdInStdOut,
                WASI_STDOUT_FD to stdInStdOut,
                WASI_STDERR_FD to stdErr,
            )
        }
    }
}
