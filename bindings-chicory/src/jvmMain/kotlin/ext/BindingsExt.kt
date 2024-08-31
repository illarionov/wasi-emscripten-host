/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.ext

import at.released.weh.bindings.chicory.exports.ChicoryWasmFunctionBinding
import at.released.weh.common.SinglePropertyLazyValue
import at.released.weh.host.base.binding.WasmFunctionBinding
import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.exceptions.ChicoryException
import com.dylibso.chicory.wasm.types.Value
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun Instance.functionMember(): ReadOnlyProperty<Any?, WasmFunctionBinding> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding> {
        private val binding: SinglePropertyLazyValue<WasmFunctionBinding> = SinglePropertyLazyValue {
            try {
                val export = this@functionMember.export(it)
                ChicoryWasmFunctionBinding(export)
            } catch (ce: ChicoryException) {
                throw IllegalStateException("No member $it", ce)
            }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): WasmFunctionBinding = binding.get(property)
    }
}

internal fun Instance.optionalFunctionMember(): ReadOnlyProperty<Any?, WasmFunctionBinding?> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding?> {
        private val binding: SinglePropertyLazyValue<WasmFunctionBinding?> = SinglePropertyLazyValue {
            try {
                this@optionalFunctionMember.export(it).let(::ChicoryWasmFunctionBinding)
            } catch (@Suppress("SwallowedException") ce: ChicoryException) {
                null
            }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): WasmFunctionBinding? = binding.get(property)
    }
}

internal fun Instance.intGlobalMember(): ReadWriteProperty<Any?, Int> {
    return object : ReadWriteProperty<Any?, Int> {
        private val binding: SinglePropertyLazyValue<ExportFunction> = SinglePropertyLazyValue {
            try {
                this@intGlobalMember.export(it)
            } catch (ce: ChicoryException) {
                throw IllegalStateException("No member $it", ce)
            }
        }
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return binding.get(property).apply()[0].asInt()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            binding.get(property).apply(Value.i32(value.toLong()))
        }
    }
}

internal fun Instance.optionalIntGlobalMember(): ReadWriteProperty<Any?, Int?> {
    return object : ReadWriteProperty<Any?, Int?> {
        private val binding = SinglePropertyLazyValue {
            try {
                this@optionalIntGlobalMember.export(it)
            } catch (@Suppress("SwallowedException") ce: ChicoryException) {
                null
            }
        }
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
            return binding.get(property)?.let { it.apply()[0].asInt() }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            requireNotNull(value)
            binding.get(property)?.apply(Value.i32(value.toLong()))
        }
    }
}
