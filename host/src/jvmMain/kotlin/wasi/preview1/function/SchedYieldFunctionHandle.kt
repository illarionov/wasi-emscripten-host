/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.wasi.preview1.function

import at.released.weh.filesystem.model.Errno
import at.released.weh.filesystem.model.Errno.SUCCESS
import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunctionHandle
import at.released.weh.host.wasi.preview1.WasiHostFunction.SCHED_YIELD

public class SchedYieldFunctionHandle(
    host: EmbedderHost,
) : HostFunctionHandle(SCHED_YIELD, host) {
    public fun execute(): Errno {
        Thread.yield()
        return SUCCESS
    }
}
