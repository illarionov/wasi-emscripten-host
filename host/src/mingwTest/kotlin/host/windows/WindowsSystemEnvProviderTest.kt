/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.windows

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import kotlin.test.Test

class WindowsSystemEnvProviderTest {
    @Test
    fun env_provider_should_return_values() {
        val env = WindowsSystemEnvProvider.getSystemEnv()
        assertThat(env.get("TEMP")).isNotNull().isNotEmpty()
        assertThat(env.get("OS")).isEqualTo("Windows_NT")
    }
}
