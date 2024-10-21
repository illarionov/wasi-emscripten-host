/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.clock

public interface CputimeSource {
    public fun getClock(clock: CpuClockId): CputimeClock

    public enum class CpuClockId {
        PROCESS_CPUTIME,
        THREAD_CPUTIME,
    }

    public interface CputimeClock {
        public val isSupported: Boolean

        public fun getTimeMarkNanoseconds(): Long

        public fun getResolutionNanoseconds(): Long
    }
}
