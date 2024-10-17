/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MatchingDeclarationName")

package at.released.weh.wasi.preview1.function

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction.SCHED_YIELD
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Errno.SUCCESS
import platform.posix.sched_yield

public actual class SchedYieldFunctionHandle actual constructor(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(SCHED_YIELD, host) {
    public actual fun execute(): Errno {
        sched_yield()
        return SUCCESS
    }
}
