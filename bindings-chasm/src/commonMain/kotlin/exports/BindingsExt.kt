/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.exports

import at.released.weh.bindings.chasm.exception.ChasmErrorException
import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.common.SinglePropertyLazyValue
import at.released.weh.host.base.binding.WasmFunctionBinding
import io.github.charlietap.chasm.embedding.global.readGlobal
import io.github.charlietap.chasm.embedding.global.writeGlobal
import io.github.charlietap.chasm.executor.runtime.instance.ExternalValue
import io.github.charlietap.chasm.executor.runtime.instance.ModuleInstance
import io.github.charlietap.chasm.executor.runtime.store.Address
import io.github.charlietap.chasm.executor.runtime.store.Store
import io.github.charlietap.chasm.executor.runtime.value.NumberValue.I32
import io.github.charlietap.chasm.fold
import io.github.charlietap.chasm.onError
import kotlin.concurrent.Volatile
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun functionMember(
    store: Store,
    instance: ModuleInstance,
): ReadOnlyProperty<Any?, WasmFunctionBinding> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding> {
        @Volatile
        private var binding: SinglePropertyLazyValue<WasmFunctionBinding> = SinglePropertyLazyValue { propertyName ->
            instance.exports.find { it.name.name == propertyName }
                ?: error("Property $propertyName not found")
            ChasmFunctionBinding(store, instance, propertyName)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): WasmFunctionBinding {
            return binding.get(property)
        }
    }
}

internal fun optionalFunctionMember(
    store: Store,
    instance: ModuleInstance,
): ReadOnlyProperty<Any?, WasmFunctionBinding?> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding?> {
        @Volatile
        private var binding: SinglePropertyLazyValue<WasmFunctionBinding?> = SinglePropertyLazyValue { propertyName ->
            if (instance.exports.find { it.name.name == propertyName } != null) {
                ChasmFunctionBinding(store, instance, propertyName)
            } else {
                null
            }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): WasmFunctionBinding? {
            return binding.get(property)
        }
    }
}

internal fun intGlobalMember(
    store: Store,
    instance: ModuleInstance,
): ReadWriteProperty<Any?, Int> {
    return object : ReadWriteProperty<Any?, Int> {
        @Volatile
        private var address = SinglePropertyLazyValue { propertyName ->
            instance.exports.firstNotNullOfOrNull {
                if (it.name.name == propertyName && it.value is ExternalValue.Global) {
                    (it.value as ExternalValue.Global).address
                } else {
                    null
                }
            } ?: error("Global $propertyName not found")
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return readGlobal(store, address.get(property)).fold(
                onSuccess = { it.asInt() },
                onError = { error -> throw ChasmErrorException(error) },
            )
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            writeGlobal(store, address.get(property), I32(value))
                .onError { error -> throw ChasmErrorException(error) }
        }
    }
}

internal fun optionalIntGlobalMember(
    store: Store,
    instance: ModuleInstance,
): ReadWriteProperty<Any?, Int?> {
    return object : ReadWriteProperty<Any?, Int?> {
        @Volatile
        private var address = SinglePropertyLazyValue { propertyName ->
            instance.exports.firstNotNullOfOrNull {
                if (it.name.name == propertyName && it.value is ExternalValue.Global) {
                    (it.value as ExternalValue.Global).address
                } else {
                    null
                }
            }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
            return address.get(property)?.let { address: Address.Global ->
                readGlobal(store, address).fold(
                    onSuccess = { it.asInt() },
                    onError = { error -> throw ChasmErrorException(error) },
                )
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            require(value != null)
            address.get(property)?.let { address: Address.Global ->
                writeGlobal(store, address, I32(value))
                    .onError { error -> throw ChasmErrorException(error) }
            }
        }
    }
}
