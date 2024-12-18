/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.real.windows

import arrow.core.flatMap
import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import kotlin.test.Test
import kotlin.test.fail

class WindowsRealPathTest {
    @Test
    fun parent_should_return_parent_directory_success_case() {
        tableOf("path", "parent")
            .row<String, String?>("", null)
            .row("/", null)
            .row(" ", null)
            .row("/a/", "/")
            .row("/a/b", "/a/")
            .row("/a/b/c", "/a/b/")
            .row("""c:\""", null)
            .row("""c:\a""", """c:\""")
            .row("""c:\a\""", """c:\""")
            .forAll { path, expectedParent: String? ->
                val winPath = WindowsRealPath.create(path).getOrElse { fail("Can not create path `$path`") }
                assertThat(winPath.parent?.kString).isEqualTo(expectedParent)
            }
    }

    @Test
    fun is_directory_request_success_case() {
        tableOf("path", "isDirectoryRequest")
            .row("", false)
            .row("""\""", true)
            .row("""c:\\""", true)
            .row("""c:\""", true)
            .row("""c:\a\b""", false)
            .row("""c:\a\b\""", true)
            .row("""\\?\c:\a\b/""", false)
            .forAll { path, isDirectoryRequest ->
                val winPath = WindowsRealPath.create(path).getOrElse { fail("Can not create path `$path`") }
                assertThat(winPath.isDirectoryRequest).isEqualTo(isDirectoryRequest)
            }
    }

    @Test
    fun append_success_case() {
        tableOf("path", "appendedPath", "resultPath")
            .row("""c:\""", """.\a\b\../\c""", """c:\a\c""")
            .forAll { path, appended, expected ->
                val winPath = WindowsRealPath.create(path)
                    .flatMap { it.append(appended) }
                    .getOrElse { fail("Can not create path `$path`") }
                assertThat(winPath.kString).isEqualTo(expected)
            }
    }
}
