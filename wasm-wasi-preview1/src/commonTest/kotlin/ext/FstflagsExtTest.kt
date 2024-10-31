/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import arrow.core.getOrElse
import assertk.assertions.isEqualToWithGivenProperties
import assertk.tableOf
import at.released.weh.host.clock.Clock
import at.released.weh.wasi.preview1.type.FstflagsFlag.ATIM
import at.released.weh.wasi.preview1.type.FstflagsFlag.ATIM_NOW
import at.released.weh.wasi.preview1.type.FstflagsFlag.MTIM
import at.released.weh.wasi.preview1.type.FstflagsFlag.MTIM_NOW
import kotlin.experimental.or
import kotlin.test.Test
import kotlin.test.fail

class FstflagsExtTest {
    @Test
    fun getRequestedAtimeMtime_success_case() {
        val clock = object : Clock {
            override fun getCurrentTimeEpochNanoseconds(): Long = 100L
            override fun getResolutionNanoseconds(): Long = 1
        }
        tableOf("atime", "mtime", "fstflags", "expectedAmtime")
            .row(1L, 5L, 0.toShort(), AtimeMtimeRequest(null, null))
            .row(1L, 5L, ATIM or MTIM, AtimeMtimeRequest(1L, 5L))
            .row(1L, 5L, ATIM_NOW or MTIM_NOW, AtimeMtimeRequest(100L, 100L))
            .forAll { atime, mtime, fstFlags, expectedamtime ->
                val result = getRequestedAtimeMtime(clock, atime, mtime, fstFlags).getOrElse { fail("Unexpected $it") }
                assertk.assertThat(result)
                    .isEqualToWithGivenProperties(
                        expectedamtime,
                        AtimeMtimeRequest::atimeNanoseconds,
                        AtimeMtimeRequest::mtimeNanoseconds,
                    )
            }
    }
}
