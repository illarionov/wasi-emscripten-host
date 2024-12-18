/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.readlink.ReadLink
import at.released.weh.filesystem.path.real.windows.WindowsPathConverter.toVirtualPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.filesystem.windows.nativefunc.open.AttributeDesiredAccess.READ_ONLY
import at.released.weh.filesystem.windows.nativefunc.open.executeWithOpenFileHandle
import at.released.weh.filesystem.windows.win32api.deviceiocontrol.getReparsePoint

internal class WindowsReadLink(
    private val fsState: WindowsFileSystemState,
) : FileSystemOperationHandler<ReadLink, ReadLinkError, VirtualPath> {
    override fun invoke(input: ReadLink): Either<ReadLinkError, VirtualPath> {
        return fsState.executeWithOpenFileHandle(
            baseDirectory = input.baseDirectory,
            path = input.path,
            followSymlinks = false,
            access = READ_ONLY,
            errorMapper = { it.toReadLinkError() },
        ) { handle ->
            handle.getReparsePoint()
        }.flatMap { linkRealPath ->
            toVirtualPath(linkRealPath)
                .mapLeft { InvalidArgument(it.message) }
        }
    }

    private fun OpenError.toReadLinkError(): ReadLinkError = if (this is ReadLinkError) {
        this
    } else {
        IoError(this.message)
    }
}
