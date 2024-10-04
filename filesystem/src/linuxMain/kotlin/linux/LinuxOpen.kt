/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.linuxOpen
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.op.opencreate.Open

internal class LinuxOpen(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<Open, OpenError, FileDescriptor> {
    override fun invoke(input: Open): Either<OpenError, FileDescriptor> =
        fsState.executeWithBaseDirectoryResource(input.baseDirectory) { directoryFd ->
            linuxOpen(directoryFd, input.path, input.flags, input.mode)
                .flatMap { nativeFd -> fsState.addFile(nativeFd) }
                .map { (fd: FileDescriptor, _) -> fd }
        }
}
