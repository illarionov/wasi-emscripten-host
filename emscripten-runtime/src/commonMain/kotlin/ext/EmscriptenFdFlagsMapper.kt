/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.ext

import at.released.weh.emcripten.runtime.include.Fcntl
import at.released.weh.filesystem.model.FdFlag
import at.released.weh.filesystem.model.FdflagsType

internal object EmscriptenFdFlagsMapper {
    private val emscriptenFlagsToFdFlags = listOf(
        Fcntl.O_APPEND to FdFlag.FD_APPEND,
        Fcntl.O_NONBLOCK to FdFlag.FD_NONBLOCK,
        Fcntl.O_DSYNC to FdFlag.FD_DSYNC,
        Fcntl.O_SYNC to (FdFlag.FD_SYNC or FdFlag.FD_RSYNC),
    )

    @FdflagsType
    fun getFdFlags(emscriptenOpenFlags: Int): Int = emscriptenFlagsToFdFlags.fold(0) { mask, (emscriptenMask, fsMask) ->
        if (emscriptenOpenFlags and emscriptenMask == emscriptenMask) {
            mask or fsMask
        } else {
            mask
        }
    }
}
