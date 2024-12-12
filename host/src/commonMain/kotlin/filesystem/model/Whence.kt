/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

/**
 * The position relative to which to set the offset of the file descriptor.
 */
public enum class Whence {
    /**
     * Seek relative to start-of-file.
     */
    SET,

    /**
     * Seek relative to current position.
     */
    CUR,

    /**
     * Seek relative to end-of-file.
     */
    END,
}
