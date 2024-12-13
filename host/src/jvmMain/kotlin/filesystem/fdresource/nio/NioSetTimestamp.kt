/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.fdresource.nio

import arrow.core.Either
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.ext.asLinkOptions
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.io.path.fileAttributesView

internal fun nioSetTimestamp(
    path: Path,
    followSymlinks: Boolean,
    atimeNanoseconds: Long?,
    mtimeNanoseconds: Long?,
): Either<SetTimestampError, Unit> {
    val options = asLinkOptions(followSymlinks = followSymlinks)
    val newAtime = atimeNanoseconds?.let { FileTime.from(it, NANOSECONDS) }
    val newMtime = mtimeNanoseconds?.let { FileTime.from(it, NANOSECONDS) }
    return Either.catch {
        path.fileAttributesView<BasicFileAttributeView>(options = options).setTimes(newMtime, newAtime, null)
    }.mapLeft {
        when (it) {
            is IOException -> IoError("I/O error: ${it.message}")
            else -> throw IllegalStateException("Unexpected error", it)
        }
    }
}
