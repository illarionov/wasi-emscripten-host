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
import io.github.charlietap.chasm.embedding.exports
import io.github.charlietap.chasm.embedding.global.readGlobal
import io.github.charlietap.chasm.embedding.global.writeGlobal
import io.github.charlietap.chasm.embedding.shapes.Global
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.Value
import io.github.charlietap.chasm.embedding.shapes.fold
import io.github.charlietap.chasm.embedding.shapes.onError
import kotlin.concurrent.Volatile
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal fun functionMember(
    store: Store,
    instance: Instance,
): ReadOnlyProperty<Any?, WasmFunctionBinding> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding> {
        @Volatile
        private var binding: SinglePropertyLazyValue<WasmFunctionBinding> = SinglePropertyLazyValue { propertyName ->
            exports(instance).find { it.name == propertyName }
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
    instance: Instance,
): ReadOnlyProperty<Any?, WasmFunctionBinding?> {
    return object : ReadOnlyProperty<Any?, WasmFunctionBinding?> {
        @Volatile
        private var binding: SinglePropertyLazyValue<WasmFunctionBinding?> = SinglePropertyLazyValue { propertyName ->
            if (exports(instance).find { it.name == propertyName } != null) {
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
    instance: Instance,
): ReadWriteProperty<Any?, Int> {
    return object : ReadWriteProperty<Any?, Int> {
        @Volatile
        private var address: SinglePropertyLazyValue<Global> = SinglePropertyLazyValue { propertyName ->
            exports(instance).firstNotNullOfOrNull {
                if (it.name == propertyName && it.value is Global) {
                    it.value as Global
                } else {
                    null
                }
            } ?: error("Global $propertyName not found")
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return readGlobal(store, address.get(property)).fold(
                onSuccess = { value: Value ->
                    @Suppress("UNCHECKED_CAST")
                    (value as Value.Number<Int>).value
                },
                onError = { error -> throw ChasmErrorException(error) },
            )
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            writeGlobal(store, address.get(property), Value.Number.I32(value))
                .onError { error -> throw ChasmErrorException(error) }
        }
    }
}

internal fun optionalIntGlobalMember(
    store: Store,
    instance: Instance,
): ReadWriteProperty<Any?, Int?> {
    return object : ReadWriteProperty<Any?, Int?> {
        @Volatile
        private var address: SinglePropertyLazyValue<Global?> = SinglePropertyLazyValue { propertyName ->
            exports(instance).firstNotNullOfOrNull {
                if (it.name == propertyName && it.value is Global) {
                    it.value as Global
                } else {
                    null
                }
            }
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? {
            return address.get(property)?.let { address: Global ->
                readGlobal(store, address).fold(
                    onSuccess = { it.asInt() },
                    onError = { error -> throw ChasmErrorException(error) },
                )
            }
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            require(value != null)
            address.get(property)?.let { address: Global ->
                writeGlobal(store, address, Value.Number.I32(value))
                    .onError { error -> throw ChasmErrorException(error) }
            }
        }
    }
}
