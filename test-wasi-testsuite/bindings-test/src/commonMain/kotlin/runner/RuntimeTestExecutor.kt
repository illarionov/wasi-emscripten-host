/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.runner

import at.released.weh.host.EmbedderHost
import kotlinx.io.files.Path

public interface RuntimeTestExecutor : AutoCloseable {
    public fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int

    public fun interface Factory {
        public operator fun invoke(): RuntimeTestExecutor
    }
}
