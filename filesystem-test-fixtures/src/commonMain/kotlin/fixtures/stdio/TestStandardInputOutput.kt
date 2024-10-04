/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.test.fixtures.stdio

import at.released.weh.filesystem.stdio.StandardInputOutput

public class TestStandardInputOutput(
    override val stdinProvider: TestSourceProvider = TestSourceProvider(),
    override val stdoutProvider: TestSinkProvider = TestSinkProvider(),
    override val stderrProvider: TestSinkProvider = TestSinkProvider(),
) : StandardInputOutput
