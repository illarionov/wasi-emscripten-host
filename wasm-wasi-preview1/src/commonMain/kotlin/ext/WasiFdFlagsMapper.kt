/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.wasi.preview1.type.Fdflags
import at.released.weh.wasi.preview1.type.FdflagsFlag
import kotlin.experimental.and
import at.released.weh.filesystem.model.FdFlag as FsFdFlag
import at.released.weh.filesystem.model.FdflagsType as FsFdflagsType
import at.released.weh.wasi.preview1.type.FdflagsType as WasiFdflagsType

internal object WasiFdFlagsMapper {
    private val wasiFdflagsToFsFdflags = listOf(
        FdflagsFlag.APPEND to FsFdFlag.FD_APPEND,
        FdflagsFlag.DSYNC to FsFdFlag.FD_DSYNC,
        FdflagsFlag.NONBLOCK to FsFdFlag.FD_NONBLOCK,
        FdflagsFlag.RSYNC to FsFdFlag.FD_RSYNC,
        FdflagsFlag.SYNC to FsFdFlag.FD_SYNC,
    )

    @FsFdflagsType
    fun getFsFdlags(
        @WasiFdflagsType fdflags: Fdflags,
    ): Int = wasiFdflagsToFsFdflags.fold(0) { mask, (wasiMask, fsMask) ->
        if (fdflags and wasiMask == wasiMask) {
            mask or fsMask
        } else {
            mask
        }
    }
}
