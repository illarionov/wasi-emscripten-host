/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.dsl

public sealed class CurrentWorkingDirectoryConfig {
    /**
     * Current directory not set. Гse default value depending on other parameters
     */
    public data object Default : CurrentWorkingDirectoryConfig()

    /**
     * Current directory should not be used
     */
    public data object Inactive : CurrentWorkingDirectoryConfig()

    /**
     * Current directory set to real path
     */
    public data class Path(val path: String) : CurrentWorkingDirectoryConfig()
}
