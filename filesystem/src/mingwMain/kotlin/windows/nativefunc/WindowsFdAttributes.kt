/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.BadFileDescriptor
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.NoEntry
import at.released.weh.filesystem.error.NotCapable
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.error.TooManySymbolicLinks
import at.released.weh.filesystem.fdrights.FdRightsBlock
import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.model.Fdflags
import at.released.weh.filesystem.op.fdattributes.FdAttributesResult
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.filesystem.windows.win32api.fileinfo.FileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.fileinfo.filetype
import at.released.weh.filesystem.windows.win32api.fileinfo.getFileAttributeTagInfo
import at.released.weh.filesystem.windows.win32api.model.FileAttributes
import platform.windows.FILE_FLAG_NO_BUFFERING
import platform.windows.FILE_FLAG_WRITE_THROUGH
import platform.windows.HANDLE

internal fun windowsFdAttributes(
    handle: HANDLE,
    isInAppendMode: Boolean,
    rights: FdRightsBlock,
): Either<FdAttributesError, FdAttributesResult> = either {
    val attrTag: FileAttributeTagInfo = handle.getFileAttributeTagInfo()
        .mapLeft(StatError::toFdattributesError)
        .bind()

    @OpenFileFlagsType
    val fileStatus: Fdflags = getFdFlags(attrTag.fileAttributes, isInAppendMode)

    return FdAttributesResult(
        type = attrTag.filetype,
        flags = fileStatus,
        rights = rights.rights,
        inheritingRights = rights.rightsInheriting,
    ).right()
}

private fun getFdFlags(
    attributes: FileAttributes,
    isInAppendMode: Boolean,
): Fdflags {
    var fdflags: Fdflags = if (isInAppendMode) {
        FdFlag.FD_APPEND
    } else {
        0
    }
    // XXX are these flags available in FILE_ATTRIBUTE_TAG_INFO attributes?
    if (attributes.mask and FILE_FLAG_NO_BUFFERING.toUInt() == FILE_FLAG_NO_BUFFERING.toUInt()) {
        fdflags = fdflags or FdFlag.FD_SYNC or FdFlag.FD_RSYNC
    }
    if (attributes.mask and FILE_FLAG_WRITE_THROUGH == FILE_FLAG_WRITE_THROUGH) {
        fdflags = fdflags or FdFlag.FD_DSYNC
    }
    return fdflags
}

private fun StatError.toFdattributesError(): FdAttributesError = when (this) {
    is AccessDenied -> this
    is BadFileDescriptor -> this
    is InvalidArgument -> this
    is IoError -> this
    is NoEntry -> BadFileDescriptor(this.message)
    is NotCapable -> IoError(this.message)
    is TooManySymbolicLinks -> this
    else -> IoError(this.message)
}
