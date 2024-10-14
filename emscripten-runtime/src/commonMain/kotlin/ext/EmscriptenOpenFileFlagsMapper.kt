/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.ext

import at.released.weh.emcripten.runtime.include.Fcntl
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import at.released.weh.filesystem.op.opencreate.OpenFileFlagsType

internal object EmscriptenOpenFileFlagsMapper {
    private val emscriptenFlagsToOpenFlags = listOf(
        Fcntl.O_CREAT to OpenFileFlag.O_CREAT,
        Fcntl.O_EXCL to OpenFileFlag.O_EXCL,
        Fcntl.O_NOCTTY to OpenFileFlag.O_NOCTTY,
        Fcntl.O_TRUNC to OpenFileFlag.O_TRUNC,
        Fcntl.O_DIRECT to OpenFileFlag.O_DIRECT,
        Fcntl.O_ASYNC to OpenFileFlag.O_ASYNC,
        Fcntl.O_LARGEFILE to OpenFileFlag.O_LARGEFILE,
        Fcntl.O_DIRECTORY to OpenFileFlag.O_DIRECTORY,
        Fcntl.O_NOFOLLOW to OpenFileFlag.O_NOFOLLOW,
        Fcntl.O_NOATIME to OpenFileFlag.O_NOATIME,
        Fcntl.O_CLOEXEC to OpenFileFlag.O_CLOEXEC,
        Fcntl.O_PATH to OpenFileFlag.O_PATH,
        Fcntl.O_TMPFILE to OpenFileFlag.O_TMPFILE,
        Fcntl.O_SEARCH to OpenFileFlag.O_SEARCH,
    )

    @OpenFileFlagsType
    fun getOpenFlags(emscriptenOpenFlags: Int): Int {
        val openMode: Int = when (val emscriptenMode = emscriptenOpenFlags and Fcntl.O_ACCMODE) {
            Fcntl.O_RDONLY -> OpenFileFlag.O_RDONLY
            Fcntl.O_WRONLY -> OpenFileFlag.O_WRONLY
            Fcntl.O_RDWR -> OpenFileFlag.O_RDWR
            else -> error("Unexpected mode 0x${emscriptenMode.toString(16)}")
        }

        return emscriptenFlagsToOpenFlags.fold(openMode) { mask, (emscriptenFlag, fsOpenFlag) ->
            if (emscriptenOpenFlags and emscriptenFlag == emscriptenFlag) {
                mask or fsOpenFlag
            } else {
                mask
            }
        }
    }
}
