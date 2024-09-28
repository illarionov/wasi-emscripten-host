/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test

import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiSuiteTestExecutor
import kotlinx.io.files.Path

public abstract class WasiTestSuiteBaseTest(
    public val wasiTestsRoot: Path,
    public val wasmRuntimeExecutorFactory: RuntimeTestExecutor.Factory,
) {
    protected fun runTest(
        testName: String,
    ) {
        wasmRuntimeExecutorFactory().use { executor ->
            WasiSuiteTestExecutor(wasiTestsRoot, testName, executor).runTest()
        }
    }
}
