/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.checkaccess

import at.released.weh.filesystem.error.CheckAccessError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.op.FileSystemOperation
import at.released.weh.filesystem.path.virtual.VirtualPath

public data class CheckAccess(
    public val path: VirtualPath,
    public val baseDirectory: BaseDirectory = BaseDirectory.CurrentWorkingDirectory,
    public val mode: Set<FileAccessibilityCheck>,
    public val useEffectiveUserId: Boolean = false,
    public val followSymlinks: Boolean = true,
) {
    public companion object : FileSystemOperation<CheckAccess, CheckAccessError, Unit> {
        override val tag: String = "checkaccess"
    }
}
