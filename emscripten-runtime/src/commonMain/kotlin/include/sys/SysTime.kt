/*
 * Copyright 2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.emcripten.runtime.include.sys

import at.released.weh.common.api.typedef.WasiEmscriptenHostIntDef
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * Timers from <sys/time.h>
 */
@WasiEmscriptenHostIntDef(
    flag = true,
    value = [
        SysIntervalTimerValue.ITIMER_REAL,
        SysIntervalTimerValue.ITIMER_VIRTUAL,
        SysIntervalTimerValue.ITIMER_PROF,
    ],
)
@Retention(SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
)
public annotation class SysIntervalTimer

public object SysIntervalTimerValue {
    public const val ITIMER_REAL: Int = 0
    public const val ITIMER_VIRTUAL: Int = 1
    public const val ITIMER_PROF: Int = 2
}
