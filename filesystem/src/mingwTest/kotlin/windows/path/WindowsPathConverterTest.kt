/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows.path

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.path.virtual.VirtualPath
import kotlin.test.Test
import kotlin.test.fail

class WindowsPathConverterTest {
    @Test
    fun convertToRealPath_success_case() {
        tableOf("virtualPath", "realPath")
            .row("/", """\??\\""")
            .row("tmp", """tmp""")
            .row("tmp/", """tmp\""")
            .row("tmp/a/../.", """tmp\a\..\.""")
            .forAll { path, expectedRealPath ->
                val realPath: String = VirtualPath.of(path)
                    .map { WindowsPathConverter.convertToRealPath(it) }
                    .getOrElse { fail("Can not create convert to real path for `$path`") }
                assertThat(realPath).isEqualTo(expectedRealPath)
            }
    }

    @Test
    fun generatePreopenedDirectoryVirtualPath_success_case() {
        tableOf("realPath", "virtualPath")
            .row("""c:\""", "/c/")
            .row("""tmp""", "tmp")
            .row("""d:\tmp\""", "/d/tmp/")
            .row("""tmp\a\..\.""", "tmp/a/../.")
            .forAll { realPath, expectedVirtualPath ->
                val virtualPath: VirtualPath = WindowsPathConverter.generatePreopenedDirectoryVirtualPath(realPath)
                    .getOrElse { fail("Can not create virtual path for `$realPath`") }
                assertThat(virtualPath.toString()).isEqualTo(expectedVirtualPath)
            }
    }
}
