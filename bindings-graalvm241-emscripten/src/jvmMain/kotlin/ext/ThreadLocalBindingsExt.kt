/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.ext

import at.released.weh.bindings.graalvm241.exports.GraalWasmFunctionBinding
import at.released.weh.common.SinglePropertyLazyValue
import at.released.weh.wasm.core.WasmFunctionBinding
import org.graalvm.polyglot.Value
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun (() -> Value).functionMember(): ReadOnlyProperty<Any?, WasmFunctionBinding> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding> {
        private val binding: SinglePropertyThreadLocalLazyValue<WasmFunctionBinding> =
            SinglePropertyThreadLocalLazyValue {
                val export = this@functionMember().getMember(it) ?: error("No member $it")
                GraalWasmFunctionBinding(export)
            }

        override fun getValue(thisRef: Any?, property: KProperty<*>): WasmFunctionBinding {
            return binding.get(property)
        }
    }
}

internal fun (() -> Value).optionalFunctionMember(): ReadOnlyProperty<Any?, WasmFunctionBinding?> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding?> {
        private val binding: SinglePropertyThreadLocalLazyValue<WasmFunctionBinding?> =
            SinglePropertyThreadLocalLazyValue {
                this@optionalFunctionMember().getMember(it)?.let(::GraalWasmFunctionBinding)
            }

        override fun getValue(thisRef: Any?, property: KProperty<*>): WasmFunctionBinding? {
            return binding.get(property)
        }
    }
}

internal fun (() -> Value).intGlobalMember(): ReadWriteProperty<Any?, Int> {
    return object : ReadWriteProperty<Any?, Int> {
        val lazyValue = SinglePropertyLazyValue {
            this@intGlobalMember().getMember(it) ?: error("No member $it")
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return lazyValue.get(property).asInt()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            lazyValue.get(property).setArrayElement(0, value) // TODO: check
        }
    }
}

internal fun (() -> Value).optionalIntGlobalMember(): ReadWriteProperty<Any?, Int?> {
    return object : ReadWriteProperty<Any?, Int?> {
        val lazyValue = SinglePropertyLazyValue { this@optionalIntGlobalMember().getMember(it) }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
            return lazyValue.get(property)?.asInt()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            requireNotNull(value)
            lazyValue.get(property)?.setArrayElement(0, value) // TODO: check
        }
    }
}
