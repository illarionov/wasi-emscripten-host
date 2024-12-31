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
import java.lang.Double.longBitsToDouble
import java.lang.Float.floatToRawIntBits
import java.lang.Float.intBitsToFloat

internal class ChicoryWasmFunctionBinding(
    private val func: ExportFunction,
) : WasmFunctionBinding {
    override fun executeVoid(vararg args: Any?) {
        func.apply(*args.argsToValues())
    }

    override fun executeForInt(vararg args: Any?): Int = func.apply(*args.argsToValues())[0].toInt()
    override fun executeForLong(vararg args: Any?): Long = func.apply(*args.argsToValues())[0]
    override fun executeForFloat(vararg args: Any?): Float = intBitsToFloat(func.apply(*args.argsToValues())[0].toInt())
    override fun executeForDouble(vararg args: Any?): Double = longBitsToDouble(func.apply(*args.argsToValues())[0])

    @IntWasmPtr
    override fun executeForPtr(vararg args: Any?): WasmPtr = func.apply(*args.argsToValues())[0].toInt()

    private fun Array<out Any?>.argsToValues(): LongArray {
        return if (this.isEmpty()) {
            LongArray(0)
        } else {
            LongArray(this.size) { idx ->
                when (val arg = this[idx]) {
                    is Int -> arg.toLong()
                    is UInt -> arg.toLong()
                    is Long -> arg.toLong()
                    is ULong -> arg.toLong()
                    is Float -> floatToRawIntBits(arg).toLong()
                    is Double -> java.lang.Double.doubleToRawLongBits(arg)
                    else -> error("Unsupported argument type $arg")
                }
            }
        }
    }
}
