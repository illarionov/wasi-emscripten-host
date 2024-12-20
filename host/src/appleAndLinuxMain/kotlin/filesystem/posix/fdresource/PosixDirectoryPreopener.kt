/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.fdresource

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.model.FdflagsType
import at.released.weh.filesystem.model.FileMode
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.path.real.posix.PosixRealPath
import at.released.weh.filesystem.path.virtual.VirtualPath
import at.released.weh.filesystem.posix.NativeDirectoryFd
import at.released.weh.filesystem.posix.NativeDirectoryFd.Companion.CURRENT_WORKING_DIRECTORY
import at.released.weh.filesystem.posix.nativefunc.getCurrentWorkingDirectoryVirtualPath
import at.released.weh.filesystem.posix.nativefunc.posixClose
import at.released.weh.filesystem.preopened.BatchDirectoryOpener
import at.released.weh.filesystem.preopened.PreopenedDirectory

internal class PosixDirectoryPreopener(
    private val posixOpen: PosixOpenCommand,
) : BatchDirectoryOpener<PosixRealPath, PosixDirectoryChannel>(
    pathFactory = PosixRealPath,
) {
    // TODO: check why if this function is not overridden, then it is not available from the Linux target
    // ("Cannot access 'preopen': it is invisible (private in a supertype)")
    override fun preopen(
        currentWorkingDirectoryPath: String?,
        preopenedDirectories: List<PreopenedDirectory>,
    ): Either<DirectoryOpenError, PreopenedDirectories<PosixDirectoryChannel>> {
        return super.preopen(currentWorkingDirectoryPath, preopenedDirectories)
    }

    override fun preopenDirectory(
        path: PosixRealPath,
        virtualPath: VirtualPath,
        baseDirectoryFd: PosixDirectoryChannel?,
    ): Either<OpenError, PosixDirectoryChannel> {
        val cwdFd = baseDirectoryFd?.nativeFd ?: CURRENT_WORKING_DIRECTORY
        val absoluteVirtualPath: VirtualPath = if (virtualPath != CURRENT_DIRECTORY_VIRTUAL_PATH) {
            virtualPath
        } else {
            getCurrentWorkingDirectoryVirtualPath().getOrElse { return it.left() }
        }

        return posixOpen(
            baseDirectoryFd = cwdFd,
            path = path,
            flags = OpenFileFlag.O_PATH,
            fdFlags = 0,
            mode = 0,
        ).map { nativeFd: Int ->
            PosixDirectoryChannel(
                nativeFd = NativeDirectoryFd(nativeFd),
                isPreopened = true,
                virtualPath = absoluteVirtualPath,
                rights = FdRightsBlock.DIRECTORY_BASE_RIGHTS_BLOCK,
            )
        }
    }

    override fun closeResource(resource: PosixDirectoryChannel): Either<CloseError, Unit> {
        return posixClose(resource.nativeFd)
    }

    internal fun interface PosixOpenCommand {
        operator fun invoke(
            baseDirectoryFd: NativeDirectoryFd,
            path: PosixRealPath,
            @OpenFileFlagsType flags: OpenFileFlags,
            @FdflagsType fdFlags: Fdflags,
            @FileMode mode: Int?,
        ): Either<OpenError, Int>
    }
}
