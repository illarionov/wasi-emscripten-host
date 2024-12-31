/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.exports

import at.released.weh.bindings.chasm.exception.ChasmErrorException
import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.bindings.chasm.ext.asLong
import at.released.weh.bindings.chasm.ext.asWasmAddr
import at.released.weh.bindings.chasm.ext.orThrow
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmFunctionBinding
import at.released.weh.wasm.core.WasmPtr
import io.github.charlietap.chasm.embedding.error.ChasmError
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Value
import io.github.charlietap.chasm.embedding.shapes.fold

internal class ChasmFunctionBinding(
    private val store: Store,
    private val instance: Instance,
    private val name: String,
) : WasmFunctionBinding {
    override fun executeVoid(vararg args: Any?) {
        invoke(store, instance, name, args.argsToValues()).orThrow()
    }

    override fun executeForInt(vararg args: Any?): Int = invoke(store, instance, name, args.argsToValues())
        .fold(
            { it[0].asInt() },
            ::throwOnError,
        )

    override fun executeForLong(vararg args: Any?): Long = invoke(store, instance, name, args.argsToValues())
        .fold(
            { it[0].asLong() },
            ::throwOnError,
        )

    override fun executeForFloat(vararg args: Any?): Float = invoke(store, instance, name, args.argsToValues())
        .fold(
            {
                @Suppress("UNCHECKED_CAST")
                (it[0] as Value.Number<Float>).value
            },
            ::throwOnError,
        )

    override fun executeForDouble(vararg args: Any?): Double = invoke(store, instance, name, args.argsToValues())
        .fold(
            {
                @Suppress("UNCHECKED_CAST")
                (it[0] as Value.Number<Double>).value
            },
            ::throwOnError,
        )

    @IntWasmPtr
    override fun executeForPtr(vararg args: Any?): WasmPtr = invoke(store, instance, name, args.argsToValues())
        .fold(
            { it[0].asWasmAddr() },
            ::throwOnError,
        )
}

private fun Array<out Any?>.argsToValues(): List<Value> {
    return if (this.isEmpty()) {
        emptyList()
    } else {
        List(this.size) { idx ->
            when (val arg = this[idx]) {
                is Int -> Value.Number.I32(arg)
                is UInt -> Value.Number.I32(arg.toInt())
                is Long -> Value.Number.I64(arg.toLong())
                is ULong -> Value.Number.I64(arg.toLong())
                is Float -> Value.Number.F32(arg.toFloat())
                is Double -> Value.Number.F64(arg.toDouble())
                else -> error("Unsupported argument type $arg")
            }
        }
    }
}

private fun throwOnError(error: ChasmError.ExecutionError): Nothing = throw ChasmErrorException(error)
