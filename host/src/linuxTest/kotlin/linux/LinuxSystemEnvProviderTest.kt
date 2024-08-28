/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.linux

import assertk.all
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isTrue
import platform.posix.setenv
import platform.posix.unsetenv
import kotlin.test.Test

class LinuxSystemEnvProviderTest {
    @Test
    fun env_provider_should_return_values() {
        val testEnvVarKey = "TESTENVVARKEY"
        val testEnvVarValue = " test = value"
        setenv(testEnvVarKey, testEnvVarValue, 1)
        try {
            val env = LinuxSystemEnvProvider.getSystemEnv()
            assertThat(env).all {
                contains(testEnvVarKey, testEnvVarValue)
                transform { it.containsKey("USER") }.isTrue()
            }
        } finally {
            unsetenv(testEnvVarKey)
        }
    }
}
