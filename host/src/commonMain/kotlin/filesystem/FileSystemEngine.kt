/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.dsl.FileSystemCommonConfig
import at.released.weh.filesystem.dsl.FileSystemEngineConfig

public interface FileSystemEngine<E : FileSystemEngineConfig> {
    @InternalWasiEmscriptenHostApi
    public fun create(
        commonConfig: FileSystemCommonConfig,
        engineConfig: E.() -> Unit,
    ): FileSystem
}
