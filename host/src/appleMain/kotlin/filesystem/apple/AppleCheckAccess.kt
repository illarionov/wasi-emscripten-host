/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleCheckAccess
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.checkaccess.CheckAccess

internal class AppleCheckAccess(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<CheckAccess, CheckAccessError, Unit> {
    override fun invoke(input: CheckAccess): Either<CheckAccessError, Unit> =
        fsState.executeWithPath(input.path, input.baseDirectory) { realPath, baseDirectory ->
            appleCheckAccess(
                path = realPath,
                baseDirectoryFd = baseDirectory,
                mode = input.mode,
                useEffectiveUserId = input.useEffectiveUserId,
                followSymlinks = input.followSymlinks,
            )
        }
}
