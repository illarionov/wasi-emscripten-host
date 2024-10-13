/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.linux.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.tableOf
import at.released.weh.filesystem.op.opencreate.OpenFileFlag
import kotlin.test.Test
import platform.posix.O_CREAT as POSIX_O_CREAT
import platform.posix.O_RDONLY as POSIX_O_RDONLY
import platform.posix.O_RDWR as POSIX_O_RDWR
import platform.posix.O_TRUNC as POSIX_O_TRUNC

class OpenFileFlagsMapperTest {
    private val testMasks = tableOf("fsMask", "linuxMask")
        .row(
            OpenFileFlag.O_RDWR or OpenFileFlag.O_CREAT,
            POSIX_O_RDWR or POSIX_O_CREAT,
        )
        .row(
            OpenFileFlag.O_RDONLY,
            POSIX_O_RDONLY,
        )
        .row(
            OpenFileFlag.O_RDWR or OpenFileFlag.O_TRUNC,
            POSIX_O_RDWR or POSIX_O_TRUNC,
        )

    @Test
    fun openFileFlagsToLinuxMask_should_generate_correct_mask() {
        testMasks.forAll { openFileFlags, expectedLinuxMask ->
            val linuxMask: ULong = openFileFlagsToLinuxMask((openFileFlags))
            assertThat(linuxMask).isEqualTo(expectedLinuxMask.toULong())
        }
    }

    @Test
    fun linuxMaskToOpenFileFlags_should_generate_correct_mask() {
        testMasks.forAll { expectedOpenFileFlags, linuxMask ->
            val openFileFlags: Int = linuxMaskToOpenFileFlags(linuxMask)
            assertThat(openFileFlags).isEqualTo(expectedOpenFileFlags)
        }
    }
}
