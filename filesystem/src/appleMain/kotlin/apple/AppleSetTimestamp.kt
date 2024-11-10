/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.apple

import arrow.core.Either
import at.released.weh.filesystem.apple.nativefunc.appleSetTimestamp
import at.released.weh.filesystem.error.SetTimestampError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.op.settimestamp.SetTimestamp

internal class AppleSetTimestamp(
    private val fsState: AppleFileSystemState,
) : FileSystemOperationHandler<SetTimestamp, SetTimestampError, Unit> {
    override fun invoke(
        input: SetTimestamp,
    ): Either<SetTimestampError, Unit> = fsState.executeWithBaseDirectoryResource(input.baseDirectory) {
        appleSetTimestamp(it, input.path, input.atimeNanoseconds, input.mtimeNanoseconds, input.followSymlinks)
    }
}
