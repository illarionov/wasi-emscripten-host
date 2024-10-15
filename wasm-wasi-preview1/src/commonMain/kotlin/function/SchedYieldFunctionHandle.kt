/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.type.Errno

public expect class SchedYieldFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle {
    public fun execute(): Errno
}
