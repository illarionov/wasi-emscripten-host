/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readlink

import at.released.weh.filesystem.error.ReadLinkError
import at.released.weh.filesystem.model.BaseDirectory
import at.released.weh.filesystem.model.BaseDirectory.CurrentWorkingDirectory
import at.released.weh.filesystem.op.FileSystemOperation

public data class ReadLink(
    public val path: String,
    public val baseDirectory: BaseDirectory = CurrentWorkingDirectory,
) {
    // TODO: VirtualPath
    public companion object : FileSystemOperation<ReadLink, ReadLinkError, String> {
        override val tag: String = "readlink"
    }
}
