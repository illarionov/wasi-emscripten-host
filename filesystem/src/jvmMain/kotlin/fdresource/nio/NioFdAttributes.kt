/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.raise.either
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.ext.toFiletype
import at.released.weh.filesystem.model.Filetype
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.DIRECTORY_BASE_RIGHTS
import at.released.weh.filesystem.op.fdattributes.FdRightsFlag.FILE_BASE_RIGHTS
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.readAttributes

internal object NioFdAttributes {
    fun getFileFdAttributes(
        channel: NioFileChannel,
    ): Either<FdAttributesError, FdAttributesResult> = either {
        val fileType = channel.path.readFileType().bind()
        check(fileType != Filetype.DIRECTORY)

        FdAttributesResult(
            type = fileType,
            flags = channel.fdFlags,
            rights = FILE_BASE_RIGHTS,
            inheritingRights = FILE_BASE_RIGHTS,
        )
    }

    fun getDirectoryFdAttributes(
        path: Path,
    ): Either<FdAttributesError, FdAttributesResult> = either {
        val fileType = path.readFileType().bind()
        check(fileType == Filetype.DIRECTORY)

        FdAttributesResult(
            type = fileType,
            flags = 0,
            rights = DIRECTORY_BASE_RIGHTS or FILE_BASE_RIGHTS,
            inheritingRights = DIRECTORY_BASE_RIGHTS or FILE_BASE_RIGHTS,
        )
    }

    private fun Path.readFileType(): Either<FdAttributesError, Filetype> = Either.catch {
        readAttributes<BasicFileAttributes>()
    }
        .mapLeft(Throwable::readAttributesToFdAttributesError)
        .map(BasicFileAttributes::toFiletype)
}

private fun Throwable.readAttributesToFdAttributesError(): FdAttributesError = when (this) {
    is UnsupportedOperationException -> AccessDenied("Can not get BasicFileAttributeView")
    is IOException -> IoError("Can not read attributes: $message")
    is SecurityException -> AccessDenied("Can not read attributes: $message")
    else -> throw IllegalStateException("Unexpected error", this)
}
