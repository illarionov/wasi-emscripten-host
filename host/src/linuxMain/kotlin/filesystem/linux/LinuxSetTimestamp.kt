/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.native.linuxSetTimestamp
import at.released.weh.filesystem.op.settimestamp.SetTimestamp
import at.released.weh.filesystem.path.ResolvePathError
import at.released.weh.filesystem.path.toResolveRelativePathErrors
import at.released.weh.filesystem.posix.fdresource.FileSystemActionExecutor

internal class LinuxSetTimestamp(
    private val fsExecutor: FileSystemActionExecutor,
) : FileSystemOperationHandler<SetTimestamp, SetTimestampError, Unit> {
    override fun invoke(
        input: SetTimestamp,
    ): Either<SetTimestampError, Unit> = fsExecutor.executeWithPath(
        input.path,
        input.baseDirectory,
        ResolvePathError::toResolveRelativePathErrors,
    ) { realPath, realBaseDirectory ->
        linuxSetTimestamp(
            realBaseDirectory.nativeFd,
            realPath,
            input.atimeNanoseconds,
            input.mtimeNanoseconds,
            input.followSymlinks,
        )
    }
}
