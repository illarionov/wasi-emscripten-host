/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.exports

import at.released.weh.bindings.chasm.exception.ChasmErrorException
import at.released.weh.bindings.chasm.ext.orThrow
import at.released.weh.host.base.WasmPtr
import at.released.weh.host.base.binding.WasmFunctionBinding
import io.github.charlietap.chasm.embedding.invoke
import io.github.charlietap.chasm.error.ChasmError
import io.github.charlietap.chasm.executor.runtime.instance.ModuleInstance
import io.github.charlietap.chasm.executor.runtime.store.Store
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.F32
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.F64
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.I32
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.I64
import io.github.charlietap.chasm.fold

internal class ChasmFunctionBinding(
    private val store: Store,
    private val instance: ModuleInstance,
    private val name: String,
) : WasmFunctionBinding {
    override fun executeVoid(vararg args: Any?) {
        invoke(store, instance, name, args.argsToValues()).orThrow()
    }

    override fun executeForInt(vararg args: Any?): Int = invoke(store, instance, name, args.argsToValues())
        .fold(
            { (it[0] as I32).value },
            ::throwOnError,
        )

    override fun executeForLong(vararg args: Any?): Long = invoke(store, instance, name, args.argsToValues())
        .fold(
            { (it[0] as I64).value },
            ::throwOnError,
        )

    override fun executeForFloat(vararg args: Any?): Float = invoke(store, instance, name, args.argsToValues())
        .fold(
            { (it[0] as F32).value },
            ::throwOnError,
        )

    override fun executeForDouble(vararg args: Any?): Double = invoke(store, instance, name, args.argsToValues())
        .fold(
            { (it[0] as F64).value },
            ::throwOnError,
        )

    override fun <P> executeForPtr(vararg args: Any?): WasmPtr<P> = invoke(store, instance, name, args.argsToValues())
        .fold(
            { WasmPtr((it[0] as I32).value) },
            ::throwOnError,
        )
}

private fun Array<out Any?>.argsToValues(): List<ExecutionValue> {
    return if (this.isEmpty()) {
        emptyList()
    } else {
        List(this.size) { idx ->
            when (val arg = this[idx]) {
                is Int -> I32(arg)
                is UInt -> I32(arg.toInt())
                is Long -> I64(arg.toLong())
                is ULong -> I64(arg.toLong())
                is Float -> F32(arg.toFloat())
                is Double -> F64(arg.toDouble())
                else -> error("Unsupported argument type $arg")
            }
        }
    }
}

private fun throwOnError(error: ChasmError.ExecutionError): Nothing = throw ChasmErrorException(error)
