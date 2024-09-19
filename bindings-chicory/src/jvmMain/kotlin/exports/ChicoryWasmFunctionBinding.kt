/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("SpreadOperator")

package at.released.weh.bindings.chicory.exports

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmFunctionBinding
import at.released.weh.wasm.core.WasmPtr
import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.wasm.types.Value

internal class ChicoryWasmFunctionBinding(
    private val func: ExportFunction,
) : WasmFunctionBinding {
    override fun executeVoid(vararg args: Any?) {
        func.apply(*args.argsToValues())
    }

    override fun executeForInt(vararg args: Any?): Int = func.apply(*args.argsToValues())[0].asInt()
    override fun executeForLong(vararg args: Any?): Long = func.apply(*args.argsToValues())[0].asLong()
    override fun executeForFloat(vararg args: Any?): Float = func.apply(*args.argsToValues())[0].asFloat()
    override fun executeForDouble(vararg args: Any?): Double = func.apply(*args.argsToValues())[0].asDouble()

    @IntWasmPtr
    override fun executeForPtr(vararg args: Any?): WasmPtr = func.apply(*args.argsToValues())[0].asInt()

    private fun Array<out Any?>.argsToValues(): Array<Value> {
        return if (this.isEmpty()) {
            emptyArray()
        } else {
            Array(this.size) { idx ->
                when (val arg = this[idx]) {
                    is Int -> Value.i32(arg.toLong())
                    is UInt -> Value.i32(arg.toLong())
                    is Long -> Value.i64(arg.toLong())
                    is ULong -> Value.i64(arg.toLong())
                    is Float -> Value.fromFloat(arg)
                    is Double -> Value.fromDouble(arg)
                    else -> error("Unsupported argument type $arg")
                }
            }
        }
    }
}
