/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chasm.exports

import at.released.weh.bindings.chasm.exception.ChasmErrorException
import at.released.weh.bindings.chasm.ext.asInt
import at.released.weh.wasm.core.WasmFunctionBinding
import io.github.charlietap.chasm.embedding.exports
import io.github.charlietap.chasm.embedding.global.readGlobal
import io.github.charlietap.chasm.embedding.global.writeGlobal
import io.github.charlietap.chasm.embedding.shapes.Export
import io.github.charlietap.chasm.embedding.shapes.Function
import io.github.charlietap.chasm.embedding.shapes.Global
import io.github.charlietap.chasm.embedding.shapes.Instance
import io.github.charlietap.chasm.embedding.shapes.Store
import io.github.charlietap.chasm.embedding.shapes.fold
import io.github.charlietap.chasm.embedding.shapes.onError
import io.github.charlietap.chasm.executor.runtime.value.ExecutionValue
import io.github.charlietap.chasm.executor.runtime.value.NumberValue
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class ChasmFunctionBindings(
    private val store: Store,
    private val instance: Instance,
    private val exportNames: Set<String>,
) {
    private val exports: Map<String, ChasmFunctionBinding> = exports(instance)
        .filter { it.name in exportNames && it.value is Function }
        .associateBy(Export::name) {
            ChasmFunctionBinding(store, instance, it.name)
        }
    val optional: ReadOnlyProperty<Any?, WasmFunctionBinding?> = ReadOnlyProperty { _, property ->
        exports[property.name]
    }
    val required: ReadOnlyProperty<Any?, WasmFunctionBinding> = ReadOnlyProperty { _, property ->
        getValue(property.name)
    }

    operator fun get(name: String): ChasmFunctionBinding? = exports[name]

    fun getValue(name: String): ChasmFunctionBinding = exports.getOrElse(name) {
        error("Required export `$name` not found")
    }
}

internal class ChasmIntGlobalsBindings(
    private val store: Store,
    instance: Instance,
    private val exportNames: Set<String>,
) {
    private val globals: Map<String, Global> = exports(instance)
        .filter { it.name in exportNames && it.value is Global }
        .associateBy(Export::name) { it.value as Global }
    val optional: ReadWriteProperty<Any?, Int?> = object : ReadWriteProperty<Any?, Int?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? = globals[property.name]?.let { global ->
            readGlobal(store, global).fold(
                onSuccess = ExecutionValue::asInt,
                onError = { error -> throw ChasmErrorException(error) },
            )
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            checkNotNull(value) { "Can not set global ${property.name} to null" }
            globals[property.name]?.let { global ->
                writeGlobal(store, global, NumberValue.I32(value))
                    .onError { error -> throw ChasmErrorException(error) }
            }
        }
    }
    val required: ReadWriteProperty<Any?, Int> = object : ReadWriteProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            val global = getGlobal(property.name)
            return readGlobal(store, global).fold(
                onSuccess = ExecutionValue::asInt,
                onError = { error -> throw ChasmErrorException(error) },
            )
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            val global = getGlobal(property.name)
            writeGlobal(store, global, NumberValue.I32(value))
                .onError { error -> throw ChasmErrorException(error) }
        }

        private fun getGlobal(name: String): Global = globals.getOrElse(name) {
            error("Global $name not exported")
        }
    }
}
