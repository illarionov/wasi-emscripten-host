/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

import at.released.weh.common.api.typedef.WasiEmscriptenHostIntDef
import at.released.weh.filesystem.model.FdFlag.FD_APPEND
import at.released.weh.filesystem.model.FdFlag.FD_DSYNC
import at.released.weh.filesystem.model.FdFlag.FD_NONBLOCK
import at.released.weh.filesystem.model.FdFlag.FD_RSYNC
import at.released.weh.filesystem.model.FdFlag.FD_SYNC
import kotlin.annotation.AnnotationRetention.SOURCE

public typealias Fdflags = Int

@WasiEmscriptenHostIntDef(
    flag = true,
    value = [
        FD_APPEND,
        FD_DSYNC,
        FD_NONBLOCK,
        FD_SYNC,
        FD_RSYNC,
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
public annotation class FdflagsType

@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
public object FdFlag {
    public const val FD_APPEND: Int = 0x01
    public const val FD_DSYNC: Int = 0x02
    public const val FD_NONBLOCK: Int = 0x04
    public const val FD_SYNC: Int = 0x08
    public const val FD_RSYNC: Int = 0x10
}
