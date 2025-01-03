/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.stdio

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.SUCCESS
import kotlin.jvm.JvmStatic

@WasiEmscriptenHostDataModel
public class StdioPollEvent(
    public val errno: FileSystemErrno = SUCCESS,
    public val bytesAvailable: Long = 0,
    public val isHangup: Boolean = false,
) {
    public companion object {
        @JvmStatic
        public val STDIO_POLL_EVENT_SUCCESS: StdioPollEvent = StdioPollEvent(
            errno = SUCCESS,
            bytesAvailable = 0,
            isHangup = false,
        )
    }
}
