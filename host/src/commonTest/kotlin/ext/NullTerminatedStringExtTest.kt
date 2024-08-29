/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.test.io.bootstrap.TestEnvironment
import kotlinx.io.readByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NullTerminatedStringExtTest {
    @BeforeTest
    fun setup() {
        TestEnvironment.prepare()
    }

    @Test
    fun encodeToNullTerminatedBuffer_test() {
        // TODO: correctly truncate on utf-8 character border
        tableOf("string", "maxDstSize", "result")
            .row("", 1, byteArrayOf(0))
            .row("", 2, byteArrayOf(0))
            .row("a", 1, byteArrayOf(0))
            .row("a", 2, byteArrayOf(97, 0))
            .row("a", 3, byteArrayOf(97, 0))
            .row("ab", 1, byteArrayOf(0))
            .row("ab", 2, byteArrayOf(97, 0))
            .row("ab", 3, byteArrayOf(97, 98, 0))
            .row("ab", 4, byteArrayOf(97, 98, 0))
            .forAll { str, maxLength, expectedBytes ->
                assertThat(str.encodeToNullTerminatedBuffer(maxLength).readByteArray())
                    .isEqualTo(expectedBytes)
            }
    }

    @Test
    fun encodeToNullTerminatedBuffer_should_fail_on_negative_arg() {
        assertFailsWith<IllegalArgumentException> { "".encodeToNullTerminatedBuffer(0) }
        assertFailsWith<IllegalArgumentException> { "".encodeToNullTerminatedBuffer(-1) }
    }

    @Test
    fun encodedNullTerminatedStringLength_test() {
        tableOf("string", "encodedLength")
            .row("", 1)
            .row("a", 2)
            .row("ab", 3)
            .forAll { str, expectedLength ->
                assertThat(str.encodedNullTerminatedStringLength())
                    .isEqualTo(expectedLength)
            }
    }
}
