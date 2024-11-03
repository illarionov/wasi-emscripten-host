/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import java.nio.file.Path

internal object NioFdAttributes {
    fun getFileFdAttributes(
        channel: NioFileChannel,
    ): Either<FdAttributesError, FdAttributesResult> = either {
        val fileType = channel.path.readFileType().bind()
        check(fileType != Filetype.DIRECTORY)

        FdAttributesResult(
            type = fileType,
            flags = channel.fdFlags,
            rights = channel.rights.rights,
            inheritingRights = channel.rights.rightsInheriting,
        )
    }

    fun getDirectoryFdAttributes(
        path: Path,
        rights: FdRightsBlock,
    ): Either<FdAttributesError, FdAttributesResult> = either {
        val fileType = path.readFileType().bind()
        check(fileType == Filetype.DIRECTORY)

        FdAttributesResult(
            type = fileType,
            flags = 0,
            rights = rights.rights,
            inheritingRights = rights.rightsInheriting,
        )
    }
}
