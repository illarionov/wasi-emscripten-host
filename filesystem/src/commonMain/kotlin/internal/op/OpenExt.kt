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
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_READ
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FD_WRITE
import at.released.weh.filesystem.op.opencreate.Open
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlag.O_DIRECTORY

internal fun checkOpenFlags(open: Open): Either<OpenError, Unit> = either {
    if (open.rights != null) {
        val readWriteMask = FD_READ or FD_WRITE
        if (open.openFlags and O_DIRECTORY == O_DIRECTORY &&
            open.rights.rights and readWriteMask == readWriteMask
        ) {
            raise(
                PathIsDirectory("opening directory with read/write rights should fail with ISDIR"),
            )
        }
    }

    val createDirectoryMask = OpenFileFlag.O_CREAT or OpenFileFlag.O_DIRECTORY
    if (open.openFlags and createDirectoryMask == createDirectoryMask) {
        raise(InvalidArgument("O_CREAT cannot be used to create directories "))
    }
}
