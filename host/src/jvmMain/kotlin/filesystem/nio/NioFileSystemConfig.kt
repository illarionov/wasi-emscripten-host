/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

import at.released.weh.filesystem.dsl.FileSystemEngineConfig
import java.nio.file.FileSystems

public class NioFileSystemConfig internal constructor() : FileSystemEngineConfig {
    public var nioFileSystem: java.nio.file.FileSystem = FileSystems.getDefault()
}
