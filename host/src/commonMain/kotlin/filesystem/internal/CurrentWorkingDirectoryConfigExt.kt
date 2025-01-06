/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal

import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig.Default
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig.Inactive
import at.released.weh.filesystem.dsl.CurrentWorkingDirectoryConfig.Path

internal fun CurrentWorkingDirectoryConfig.getDefaultPath(
    isRootAccessAllowed: Boolean,
) = when (this) {
    Default -> if (isRootAccessAllowed) {
        "."
    } else {
        null
    }
    Inactive -> null
    is Path -> path
}
