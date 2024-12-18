/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.path.virtual

import arrow.core.getOrElse
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.fail
import assertk.tableOf
import at.released.weh.filesystem.path.PathError
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isAbsolute
import at.released.weh.filesystem.path.virtual.VirtualPath.Companion.isDirectoryRequest
import kotlinx.io.bytestring.encodeToByteString
import kotlin.test.Test

class VirtualPathTest {
    @Test
    fun create_virtual_path_success_case() {
        tableOf("pathString")
            .row("tmp")
            .row("/path1")
            .row("/path1/")
            .row("/path/.")
            .row(".")
            .row("..")
            .forAll { pathString ->
                val virtualPath = VirtualPath.create(pathString).getOrElse {
                    fail("Can not create virtual path for `$pathString`")
                }
                assertThat(virtualPath.toString()).isEqualTo(pathString)
            }
    }

    @Test
    fun create_virtual_path_should_fail_on_empty_path() {
        val pathError = VirtualPath.create("").leftOrNull()
        assertThat(pathError).isNotNull().isInstanceOf<PathError.EmptyPath>()
    }

    @Test
    fun create_virtual_path_should_fail_on_zero_byte() {
        tableOf("pathString")
            .row("\u0000")
            .row("/path1\u0000")
            .row("/path1\u0000/path2")
            .forAll {
                val pathError = VirtualPath.create(it).leftOrNull()
                assertThat(pathError).isNotNull().isInstanceOf<PathError.InvalidPathFormat>()
            }
    }

    @Test
    fun virtual_path_utf8_bytestring_test_success_case() {
        tableOf("pathString")
            .row("tmp")
            .row(".")
            .row("..")
            .forAll { pathString ->
                val virtualPath = VirtualPath.create(pathString).getOrElse {
                    fail("Can not create virtual path for `$pathString`")
                }
                assertThat(virtualPath.utf8Bytes).isEqualTo(pathString.encodeToByteString())
            }
    }

    @Test
    fun virtual_path_is_directory_request_test() {
        tableOf("pathString", "isDirectoryRequest")
            .row("tmp", false)
            .row("tmp/", true)
            .row(".", false)
            .row("/", true)
            .row("/tmp", false)
            .forAll { pathString, isDirectoryRequest ->
                val virtualPath = VirtualPath.create(pathString).getOrElse {
                    fail("Can not create virtual path for `$pathString`")
                }
                assertThat(virtualPath.isDirectoryRequest()).isEqualTo(isDirectoryRequest)
            }
    }

    @Test
    fun virtual_path_is_absolute_request_test() {
        tableOf("pathString", "isAbsolute")
            .row("tmp", false)
            .row("/tmp", true)
            .row(".", false)
            .row("/", true)
            .row("/tmp", true)
            .forAll { pathString, isAbsolute ->
                val virtualPath = VirtualPath.create(pathString).getOrElse {
                    fail("Can not create virtual path for `$pathString`")
                }
                assertThat(virtualPath.isAbsolute()).isEqualTo(isAbsolute)
            }
    }
}
