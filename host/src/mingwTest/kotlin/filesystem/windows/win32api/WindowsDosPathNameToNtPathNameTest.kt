/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.win32api

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.ResolvePathError
import kotlin.test.Test
import kotlin.test.fail

class WindowsDosPathNameToNtPathNameTest {
    @Test
    fun windowsDosPathNameToNtPathName_success_case() {
        val pathResult = windowsDosPathNameToNtPathName("""C:\Windows""")
            .getOrElse { fail("Can not convert path: $it") }
        assertThat(pathResult.kString).isEqualTo("""\??\C:\Windows""")
    }

    @Test
    fun windowsDosPathNameToNtPathName_empty_path() {
        val pathResult: ResolvePathError? = windowsDosPathNameToNtPathName("""""").leftOrNull()
        assertThat(pathResult).isNotNull().isInstanceOf<PathError.EmptyPath>()
    }

    @Test
    fun windowsDosPathNameToNtPathName_invalid_path() {
        val pathResult: ResolvePathError? = windowsDosPathNameToNtPathName(""" """).leftOrNull()
        assertThat(pathResult).isNotNull().isInstanceOf<PathError.InvalidPathFormat>()
    }
}
