/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LONG_NUMERICAL_VALUES_SEPARATED")

package at.released.weh.filesystem.windows.win32api.model.errorcode

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.windows.win32api.model.errorcode.NtStatus.Severity
import kotlin.test.Test

class NtStatusTest {
    @Test
    fun ntStatus_isSuccess_should_be_true_for_success_cases() {
        tableOf("errorCode", "isSuccess")
            .row(0x00000001U, true)
            .row(0x40000000U, true)
            .row(0x80000001U, true)
            .row(NtStatus.STATUS_UNSUCCESSFUL, false)
            .forAll { errorCode, expectedIsSuccess ->
                assertThat(NtStatus(errorCode).isSuccess).isEqualTo(expectedIsSuccess)
            }
    }

    @Test
    fun ntStatus_severity() {
        tableOf("error", "severity")
            .row(0x00000001U, Severity.SUCCESS)
            .row(0x40000000U, Severity.INFORMATIONAL)
            .row(0x80000001U, Severity.WARNING)
            .row(NtStatus.STATUS_UNSUCCESSFUL, Severity.ERROR)
            .forAll { errorCode, expectedSeverity ->
                assertThat(NtStatus(errorCode).severity).isEqualTo(expectedSeverity)
            }
    }
}
