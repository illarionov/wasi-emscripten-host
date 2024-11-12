/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.readdir

import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.op.readdir.DirEntry

internal sealed class PosixReadDirResult {
    @Suppress("ConvertObjectToDataObject")
    object EndOfStream : PosixReadDirResult()
    data class Entry(val entry: DirEntry) : PosixReadDirResult()
    data class Error(val error: ReadDirError) : PosixReadDirResult()
}
