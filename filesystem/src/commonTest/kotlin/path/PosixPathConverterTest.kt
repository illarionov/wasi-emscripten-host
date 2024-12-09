/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path

import arrow.core.flatMap
import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.path.virtual.VirtualPath
import kotlin.test.Test
import kotlin.test.fail

class PosixPathConverterTest {
    @Test
    fun convert_to_real_path_success_case() {
        tableOf("virtualPath", "realPath")
            .row("/", "/")
            .row("tmp", "tmp")
            .row("tmp/", "tmp/")
            .row("tmp/a/../.", "tmp/a/../.")
            .forAll { path, expectedRealPath ->
                val realPath = VirtualPath.of(path)
                    .flatMap { PosixPathConverter.toRealPath(it) }
                    .getOrElse { fail("Can not convert to real path `$path`") }
                assertThat(realPath).isEqualTo(expectedRealPath)
            }
    }

    @Test
    fun convert_to_virtual_path_success_case() {
        tableOf("realPath", "virtualPath")
            .row("/", "/")
            .row("tmp", "tmp")
            .row("tmp/", "tmp/")
            .row("tmp/a/../.", "tmp/a/../.")
            .forAll { realPath, expectedVirtualPath ->
                val virtualPath: VirtualPath = PosixPathConverter.convertToVirtualPath(realPath)
                    .getOrElse { fail("Can not create virtual path for `$realPath`") }
                assertThat(virtualPath.toString()).isEqualTo(expectedVirtualPath)
            }
    }
}
