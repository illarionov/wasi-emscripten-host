/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

import at.released.weh.common.api.WasiEmscriptenHostDataModel

/**
 * Base path used to resolve relative paths
 */
@Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
public sealed class BaseDirectory {
    /**
     * Paths are resolved relative to the current working directory
     */
    public data object CurrentWorkingDirectory : BaseDirectory()

    /**
     * Relative paths are resolved relative to the directory associated with the file descriptor [fd]
     */
    @WasiEmscriptenHostDataModel
    public class DirectoryFd(@IntFileDescriptor public val fd: FileDescriptor) : BaseDirectory()

    public companion object
}
