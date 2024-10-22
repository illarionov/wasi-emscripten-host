/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.AccessDenied
import at.released.weh.filesystem.error.FdAttributesError
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.ext.toFiletype
import at.released.weh.filesystem.fdresource.nio.NioFileStat.ATTR_UNI_INO
import at.released.weh.filesystem.model.Filetype
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.readAttributes

internal fun Path.readFileType(): Either<FdAttributesError, Filetype> = Either.catch {
    readAttributes<BasicFileAttributes>()
}
    .mapLeft(Throwable::readAttributesToFdAttributesError)
    .map(BasicFileAttributes::toFiletype)

internal fun Path.readInode(
    followSymlinks: Boolean = true,
): Either<FdAttributesError, Long> = Either.catch {
    val linkOptions = asLinkOptions(followSymlinks)
    val attrs = readAttributes("unix:ino", options = linkOptions)
    (attrs[ATTR_UNI_INO] as? Long) ?: throw UnsupportedOperationException("No inode")
}
    .mapLeft(Throwable::readAttributesToFdAttributesError)
    .swap()
    .flatMap { _ ->
        readFileKey(followSymlinks)
            .map { it?.hashCode()?.toLong() ?: 0 }
            .swap()
    }
    .swap()

internal fun Path.readFileKey(
    followSymlinks: Boolean = true,
): Either<FdAttributesError, Any?> {
    val linkOptions = asLinkOptions(followSymlinks)
    return Either.catch {
        readAttributes<BasicFileAttributes>(options = linkOptions)
    }
        .mapLeft(Throwable::readAttributesToFdAttributesError)
        .map(BasicFileAttributes::fileKey)
}

private fun Throwable.readAttributesToFdAttributesError(): FdAttributesError = when (this) {
    is UnsupportedOperationException -> AccessDenied("Can not get BasicFileAttributeView")
    is IOException -> IoError("Can not read attributes: $message")
    is SecurityException -> AccessDenied("Can not read attributes: $message")
    else -> throw IllegalStateException("Unexpected error", this)
}
