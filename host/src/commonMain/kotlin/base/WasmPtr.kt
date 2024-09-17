/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.host.base

import androidx.annotation.IntDef
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.reflect.KClass

@IntDef(flag = false)
@Retention(SOURCE)
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPE,
    AnnotationTarget.VALUE_PARAMETER,
)
public annotation class IntWasmPtr(
    public val ref: KClass<*> = Unit::class,
)

public typealias WasmPtr = @IntWasmPtr Int

public const val WASM_SIZEOF_PTR: UInt = 4U

@IntWasmPtr
public const val C_NULL: WasmPtr = 0

@InternalWasiEmscriptenHostApi
public fun ptrIsNull(@IntWasmPtr ptr: WasmPtr): Boolean = ptr == C_NULL
