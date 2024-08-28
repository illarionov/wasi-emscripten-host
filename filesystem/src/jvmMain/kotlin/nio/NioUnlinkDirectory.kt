/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import at.released.weh.filesystem.error.NotDirectory
import at.released.weh.filesystem.error.UnlinkError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.nio.NioUnlinkFile.Companion.toUnlinkError
import at.released.weh.filesystem.nio.cwd.PathResolver
import at.released.weh.filesystem.op.unlink.UnlinkFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

internal class NioUnlinkDirectory(
    private val pathResolver: PathResolver,
) : FileSystemOperationHandler<UnlinkFile, UnlinkError, Unit> {
    @Suppress("ReturnCount")
    override fun invoke(input: UnlinkFile): Either<UnlinkError, Unit> {
        val path: Path = pathResolver.resolve(input.path, input.baseDirectory, false)
            .mapLeft { it.toUnlinkError() }
            .getOrElse { return it.left() }

        if (!path.isDirectory()) {
            return NotDirectory("`$path` is not a directory").left()
        }

        return Either.catch {
            Files.delete(path)
        }.mapLeft {
            it.toUnlinkError(path)
        }
    }
}
