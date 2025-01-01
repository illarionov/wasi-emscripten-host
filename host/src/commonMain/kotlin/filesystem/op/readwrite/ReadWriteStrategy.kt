/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.readwrite

import at.released.weh.common.api.WasiEmscriptenHostDataModel

public sealed class ReadWriteStrategy {
    /**
     * Reads from the file descriptor's current position, updating it accordingly.
     */
    public data object CurrentPosition : ReadWriteStrategy()

    /**
     * Read from the given [position] within the file without using and updating the file descriptor's offset
     */
    @WasiEmscriptenHostDataModel
    public class Position(public val position: Long) : ReadWriteStrategy()
}
