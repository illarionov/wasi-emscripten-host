/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.model

import at.released.weh.common.api.typedef.WasiEmscriptenHostIntDef
import kotlin.annotation.AnnotationRetention.SOURCE

/**
 * A file descriptor handle.
 */
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
@WasiEmscriptenHostIntDef(open = true)
public annotation class IntFileDescriptor

public typealias FileDescriptor = Int
