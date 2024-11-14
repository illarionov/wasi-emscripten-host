/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test

import at.released.weh.test.io.bootstrap.TestEnvironment
import at.released.weh.test.utils.TempFolder
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiSuiteTestExecutor
import kotlinx.io.files.Path
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal expect fun isShouldBeIgnored(ignores: Set<DynamicIgnoreTarget>): Boolean

public abstract class WasiTestSuiteBaseTest(
    public val wasiTestsRoot: Path,
    public val wasmRuntimeExecutorFactory: RuntimeTestExecutor.Factory,
) {
    private var tempFolder: TempFolder? = null

    @BeforeTest
    public fun setup() {
        TestEnvironment.prepare()
        tempFolder = TempFolder.create()
    }

    @AfterTest
    public fun cleanup() {
        try {
            tempFolder?.delete()
        } finally {
            TestEnvironment.cleanup()
        }
    }

    protected fun runTest(
        testName: String,
        ignores: Set<DynamicIgnoreTarget> = emptySet(),
    ) {
        if (isShouldBeIgnored(ignores)) {
            return
        }

        wasmRuntimeExecutorFactory().let { executor ->
            WasiSuiteTestExecutor(
                testsRoot = wasiTestsRoot,
                testName = testName,
                runtimeTestExecutor = executor,
                tempRoot = Path(tempFolder!!.path),
            ).runTest()
        }
    }
}
