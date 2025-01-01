/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.stat

import at.released.weh.common.api.WasiEmscriptenHostDataModel
import kotlin.time.Duration.Companion.seconds

@WasiEmscriptenHostDataModel
public class StructTimespec(
    public val seconds: Long,
    public val nanoseconds: Long,
) {
    override fun toString(): String {
        return "TS($seconds sec $nanoseconds nsec)"
    }

    internal companion object
}

public val StructTimespec.timeMillis: Long
    get(): Long = seconds * 1000 + nanoseconds / 1_000_000

public val StructTimespec.timeNanos: Long
    get(): Long = (this.seconds).seconds.inWholeNanoseconds + this.nanoseconds
