/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.dsl

import at.released.weh.common.api.WasiEmscriptenHostDsl
import at.released.weh.filesystem.preopened.PreopenedDirectory
import at.released.weh.filesystem.preopened.RealPath

@WasiEmscriptenHostDsl
public class DirectoryConfigBlock {
    /**
     * Specifies whether access to files outside the pre-opened directories is permitted
     */
    public var isRootAccessAllowed: Boolean = false
    public var currentWorkingDirectory: String? = null
    private val _preopenedDirectories: MutableList<PreopenedDirectory> = mutableListOf()
    public val preopenedDirectories: List<PreopenedDirectory> get() = _preopenedDirectories

    public fun addPreopenedDirectory(
        realPath: RealPath,
    ): DirectoryConfigBlock = apply {
        _preopenedDirectories.add(PreopenedDirectory(realPath))
    }

    public fun preopened(
        block: MutableList<PreopenedDirectory>.() -> Unit,
    ): Unit = block(_preopenedDirectories)
}
