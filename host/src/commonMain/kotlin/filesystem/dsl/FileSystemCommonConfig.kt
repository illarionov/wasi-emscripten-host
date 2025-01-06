/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.dsl

import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.filesystem.FileSystemInterceptor
import at.released.weh.filesystem.preopened.PreopenedDirectory

@InternalWasiEmscriptenHostApi
public interface FileSystemCommonConfig {
    public val interceptors: List<FileSystemInterceptor>
    public val stdioConfig: StandardInputOutputConfigBlock
    public val isRootAccessAllowed: Boolean
    public val currentWorkingDirectory: CurrentWorkingDirectoryConfig
    public val preopenedDirectories: List<PreopenedDirectory>
}
