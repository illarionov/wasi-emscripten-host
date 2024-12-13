/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.test.fixtures.readdir

import at.released.weh.filesystem.model.Filetype.DIRECTORY
import at.released.weh.filesystem.op.readdir.DirEntry

public object TestDirEntry {
    public val TEST_CURRENT_DIR_ENTRY: DirEntry = DirEntry(".", DIRECTORY, 117, 8)
    public val TEST_PARENT_DIR_ENTRY: DirEntry = DirEntry("..", DIRECTORY, 118, 9)
}
