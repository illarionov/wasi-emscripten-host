/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType
import at.released.weh.wasi.preview1.type.Oflags
import at.released.weh.wasi.preview1.type.OflagsFlag
import at.released.weh.wasi.preview1.type.OflagsType
import kotlin.experimental.and

internal object WasiOpenFlagsMapper {
    private val wasiOflagsToFsOpenFlags = listOf(
        OflagsFlag.CREAT to OpenFileFlag.O_CREAT,
        OflagsFlag.DIRECTORY to OpenFileFlag.O_DIRECTORY,
        OflagsFlag.EXCL to OpenFileFlag.O_EXCL,
        OflagsFlag.TRUNC to OpenFileFlag.O_TRUNC,
    )

    @OpenFileFlagsType
    fun getFsOpenFlags(
        @OflagsType wasiOpenFlags: Oflags,
    ): Int = wasiOflagsToFsOpenFlags.fold(0) { mask, (oflagMask, fsOpenFileFlagMask) ->
        if (wasiOpenFlags and oflagMask == oflagMask) {
            mask or fsOpenFileFlagMask
        } else {
            mask
        }
    }
}
