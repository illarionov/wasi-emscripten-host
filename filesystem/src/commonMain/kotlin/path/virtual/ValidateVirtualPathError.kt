/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.virtual

import at.released.weh.filesystem.error.FileSystemOperationError
import at.released.weh.filesystem.model.FileSystemErrno

public sealed class ValidateVirtualPathError : FileSystemOperationError {
    override val errno: FileSystemErrno = FileSystemErrno.INVAL

    internal data class PathIsEmpty(
        override val message: String = "Path is empty",
    ) : ValidateVirtualPathError()

    internal data class InvalidCharacters(
        override val message: String = "Path has invalid characters",
    ) : ValidateVirtualPathError()
}
