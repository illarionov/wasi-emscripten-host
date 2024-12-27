/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.op

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.OpenError
import at.released.weh.filesystem.error.PathIsDirectory
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.fdrights.FdRightsFlag.FD_READ
import at.released.weh.filesystem.fdrights.FdRightsFlag.FD_WRITE
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECTORY
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_NOFOLLOW
import at.released.weh.filesystem.op.opencreate.OpenFileFlags
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType

internal fun checkOpenFlags(
    @OpenFileFlagsType openFlags: OpenFileFlags,
    rights: FdRightsBlock?,
    isDirectoryRequested: Boolean,
): Either<OpenError, Unit> = either {
    if (rights != null) {
        val readWriteMask = FD_READ or FD_WRITE
        if ((isDirectoryRequested || openFlags and O_DIRECTORY == O_DIRECTORY) &&
            rights.rights and readWriteMask == readWriteMask
        ) {
            // See wasi-testsuite/tests/rust/src/bin/path_open_preopen.rs
            raise(
                PathIsDirectory("opening directory with read/write rights should fail with ISDIR"),
            )
        }
    }

    val createDirectoryMask = OpenFileFlag.O_CREAT or OpenFileFlag.O_DIRECTORY
    if (openFlags and createDirectoryMask == createDirectoryMask) {
        raise(InvalidArgument("O_CREAT cannot be used to create directories "))
    }
}

internal fun openFileFlagsWithFollowSymlinks(
    mask: OpenFileFlags,
    followSymlinks: Boolean,
): OpenFileFlags {
    val nativeFollowLinksMask = if (followSymlinks) {
        0
    } else {
        O_NOFOLLOW
    }

    return (mask and O_NOFOLLOW.inv()) or nativeFollowLinksMask
}
