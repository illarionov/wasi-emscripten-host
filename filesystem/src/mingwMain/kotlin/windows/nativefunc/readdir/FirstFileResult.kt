/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.nativefunc.readdir

import at.released.weh.filesystem.error.ReadDirError
import at.released.weh.filesystem.posix.readdir.ReadDirResult
import at.released.weh.filesystem.posix.readdir.ReadDirResult.Companion.readDirResult

internal class FirstFileResult(
    val firstEntry: ReadDirResult,
    val nextEntryProvider: WindowsNextEntryProvider,
) {
    operator fun component1(): ReadDirResult = firstEntry
    operator fun component2(): WindowsNextEntryProvider = nextEntryProvider

    internal companion object {
        internal fun error(readDirError: ReadDirError): FirstFileResult {
            return FirstFileResult(readDirError.readDirResult(), ClosedNextDirProvider)
        }
    }
}
