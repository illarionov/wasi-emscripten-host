/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleStat
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class AppleStat(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<Stat, StatError, StructStat> {
    override fun invoke(input: Stat): Either<StatError, StructStat> {
        return fsExecutor.executeWithPath(
            input.path,
            input.baseDirectory,
            ResolvePathError::toResolveRelativePathErrors,
        ) { realPath, baseDirectory ->
            appleStat(baseDirectory.nativeFd, realPath, input.followSymlinks)
        }
    }
}
