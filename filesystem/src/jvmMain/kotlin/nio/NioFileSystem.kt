/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.FileSystem
import at.released.weh.filesystem.FileSystemEngine
import at.released.weh.filesystem.dsl.FileSystemCommonConfig

public object NioFileSystem : FileSystemEngine<NioFileSystemConfig> {
    @InternalWasiEmscriptenHostApi
    override fun create(
        commonConfig: FileSystemCommonConfig,
        engineConfig: NioFileSystemConfig.() -> Unit,
    ): FileSystem {
        val nioConfig = NioFileSystemConfig().apply(engineConfig)
        return NioFileSystemImpl(
            javaFs = nioConfig.nioFileSystem,
            interceptors = commonConfig.interceptors,
        )
    }
}
