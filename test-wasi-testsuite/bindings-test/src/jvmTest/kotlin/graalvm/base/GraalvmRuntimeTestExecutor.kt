/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.graalvm.base

import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments

object GraalvmRuntimeTestExecutor : RuntimeTestExecutor {
    object Factory : RuntimeTestExecutor.Factory {
        override fun invoke(): RuntimeTestExecutor = GraalvmRuntimeTestExecutor
    }

    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
    ): Int {
        TODO("Not yet implemented")
    }

    override fun close() = Unit
}
