/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.IoError
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.ext.asLinkOptions
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.cwd.PathResolver.ResolvePathError
import at.released.weh.filesystem.nio.cwd.toCommonError
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import java.io.IOException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.io.path.fileAttributesView

internal class NioSetTimestamp(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<SetTimestamp, SetTimestampError, Unit> {
    override fun invoke(input: SetTimestamp): Either<SetTimestampError, Unit> {
        val path: Path = fsState.pathResolver.resolve(input.path, input.baseDirectory, false)
            .mapLeft(ResolvePathError::toCommonError)
            .getOrElse { return it.left() }
        return setTimestamp(path, input.followSymlinks, input.atimeNanoseconds, input.mtimeNanoseconds)
    }

    internal companion object {
        fun setTimestamp(
            path: Path,
            followSymlinks: Boolean,
            atimeNanoseconds: Long?,
            mtimeNanoseconds: Long?,
        ): Either<SetTimestampError, Unit> {
            val options = asLinkOptions(followSymlinks = followSymlinks)
            return Either.catch {
                path.fileAttributesView<BasicFileAttributeView>(options = options)
                    .setTimes(
                        atimeNanoseconds?.let { FileTime.from(it, NANOSECONDS) },
                        mtimeNanoseconds?.let { FileTime.from(it, NANOSECONDS) },
                        null,
                    )
            }.mapLeft {
                when (it) {
                    is IOException -> IoError("I/O error: ${it.message}")
                    else -> throw IllegalStateException("Unexpected error", it)
                }
            }
        }
    }
}
