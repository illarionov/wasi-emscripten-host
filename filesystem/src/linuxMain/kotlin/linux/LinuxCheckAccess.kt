/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux

import arrow.core.Either
import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.linuxCheckAccess
import at.released.weh.filesystem.op.checkaccess.CheckAccess

internal class LinuxCheckAccess(
    private val fsState: LinuxFileSystemState,
) : FileSystemOperationHandler<CheckAccess, CheckAccessError, Unit> {
    override fun invoke(input: CheckAccess): Either<CheckAccessError, Unit> =
        fsState.executeWithBaseDirectoryResource(input.baseDirectory) { nativeFdOrAtCwd ->
            linuxCheckAccess(
                path = input.path,
                baseDirectoryFd = nativeFdOrAtCwd,
                mode = input.mode,
                useEffectiveUserId = input.useEffectiveUserId,
                allowEmptyPath = input.allowEmptyPath,
                followSymlinks = input.followSymlinks,
            )
        }
}
