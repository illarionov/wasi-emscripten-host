/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.type

import at.released.weh.filesystem.model.Fd
import at.released.weh.host.base.WasmValueType
import at.released.weh.host.base.WasmValueTypes.I32

/**
 * The contents of a `subscription` when type is type is `eventtype::fd_read` or `eventtype::fd_write`.
 *
 * @param fileDescriptor The file descriptor on which to wait for it to become ready for reading or writing.
 */
public data class SubscriptionFdReadwrite(
    val fileDescriptor: Fd, // (field $file_descriptor $fd)
) {
    public companion object : WasiTypename {
        @WasmValueType
        public override val wasmValueType: Int = I32
    }
}
