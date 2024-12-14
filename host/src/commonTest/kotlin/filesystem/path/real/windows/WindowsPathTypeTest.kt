/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.path.real.windows.WindowsPathFixtures.WINDOWS_TEST_PATH_TYPES
import kotlin.test.Test

class WindowsPathTypeTest {
    @Test
    fun getWindowsPathType_success_case() {
        val testPathTypes: List<Pair<String, WindowsPathType>> = WINDOWS_TEST_PATH_TYPES.flatMap { (type, patches) ->
            patches.map { it to type }
        }
        tableOf("path", "expectedType")
            .row(testPathTypes[0].first, testPathTypes[0].second)
            .apply {
                testPathTypes.drop(1).forEach { row(it.first, it.second) }
            }.forAll { path, expectedType ->
                assertThat(getWindowsPathType(path)).isEqualTo(expectedType)
            }
    }
}
