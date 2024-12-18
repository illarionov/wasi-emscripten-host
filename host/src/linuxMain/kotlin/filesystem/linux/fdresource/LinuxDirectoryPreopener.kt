/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.fdresource

import arrow.core.Either
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.linux.native.linuxOpenRaw
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeDirectoryFd.Companion.CURRENT_WORKING_DIRECTORY
import at.released.weh.filesystem.preopened.BatchDirectoryOpener

internal object LinuxDirectoryPreopener : BatchDirectoryOpener<PosixRealPath, LinuxDirectoryFdResource>(
    pathFactory = PosixRealPath,
) {
    override fun preopenDirectory(
        path: PosixRealPath,
        virtualPath: VirtualPath,
        baseDirectoryFd: LinuxDirectoryFdResource?,
    ): Either<OpenError, LinuxDirectoryFdResource> {
        val cwdFd = baseDirectoryFd?.let(LinuxDirectoryFdResource::nativeFd) ?: CURRENT_WORKING_DIRECTORY
        return linuxOpenRaw(
            baseDirectoryFd = cwdFd,
            path = path,
            flags = OpenFileFlag.O_PATH,
            fdFlags = 0,
            mode = 0,
        ).map { nativeFd: Int ->
            LinuxDirectoryFdResource(
                nativeFd = NativeDirectoryFd(nativeFd),
                isPreopened = true,
                virtualPath = virtualPath,
                rights = FdRightsBlock.DIRECTORY_BASE_RIGHTS_BLOCK,
            )
        }
    }

    override fun closeResource(resource: LinuxDirectoryFdResource): Either<CloseError, Unit> {
        return resource.close()
    }
}
