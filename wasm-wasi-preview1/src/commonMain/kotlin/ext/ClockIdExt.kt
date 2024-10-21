/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.preview1.ext

import at.released.weh.host.clock.CputimeSource.CpuClockId
import at.released.weh.wasi.preview1.type.Clockid
import at.released.weh.wasi.preview1.type.Clockid.MONOTONIC
import at.released.weh.wasi.preview1.type.Clockid.PROCESS_CPUTIME_ID
import at.released.weh.wasi.preview1.type.Clockid.REALTIME
import at.released.weh.wasi.preview1.type.Clockid.THREAD_CPUTIME_ID

internal val Clockid.hostClockId: CpuClockId
    get() = when (this) {
        REALTIME, MONOTONIC -> throw IllegalArgumentException("No CPUtime for this clock")
        PROCESS_CPUTIME_ID -> CpuClockId.PROCESS_CPUTIME
        THREAD_CPUTIME_ID -> CpuClockId.THREAD_CPUTIME
    }
