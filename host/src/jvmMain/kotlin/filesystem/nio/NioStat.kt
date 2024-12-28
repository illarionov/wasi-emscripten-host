/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import arrow.core.Either
import arrow.core.flatMap
import at.released.weh.filesystem.error.StatError
import at.released.weh.filesystem.fdresource.nio.NioFileStat
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.stat.Stat
import at.released.weh.filesystem.op.stat.StructStat
import at.released.weh.filesystem.path.withResolvePathErrorAsCommonError

internal class NioStat(
    private val fsState: NioFileSystemState,
) : FileSystemOperationHandler<Stat, StatError, StructStat> {
    override fun invoke(input: Stat): Either<StatError, StructStat> =
        fsState.executeWithPath(input.baseDirectory, input.path, input.followSymlinks) { resolvePathResult ->
            resolvePathResult
                .withResolvePathErrorAsCommonError()
                .flatMap { NioFileStat.getStat(it, input.followSymlinks) }
        }
}
