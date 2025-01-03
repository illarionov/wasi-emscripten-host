/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.ext

import at.released.weh.bindings.chicory.exports.ChicoryWasmFunctionBinding
import at.released.weh.wasm.core.WasmFunctionBinding
import com.dylibso.chicory.runtime.ExportFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.wasm.ChicoryException
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class ChicoryFunctionBindings(
    private val instance: Instance,
    exportNames: Set<String>,
) {
    private val exports: Map<String, WasmFunctionBinding> = exportNames.mapNotNull { exportName ->
        try {
            instance.export(exportName)
        } catch (@Suppress("SwallowedException") ex: ChicoryException) {
            null
        } catch (@Suppress("SwallowedException", "TooGenericExceptionCaught") ex: NullPointerException) {
            null
        }?.let { exportFunction ->
            exportName to ChicoryWasmFunctionBinding(exportFunction)
        }
    }.toMap()
    val optional: ReadOnlyProperty<Any?, WasmFunctionBinding?> = ReadOnlyProperty { _, property ->
        exports[property.name]
    }
    val required: ReadOnlyProperty<Any?, WasmFunctionBinding> = ReadOnlyProperty { _, property ->
        getValue(property.name)
    }

    operator fun get(name: String): WasmFunctionBinding? = exports[name]

    fun getValue(name: String): WasmFunctionBinding = exports.getOrElse(name) {
        error("Required export `$name` not found")
    }
}

internal class ChicoryIntGlobalsBindings(
    private val instance: Instance,
    exportNames: Set<String>,
) {
    private val globals: Map<String, ExportFunction> = exportNames.mapNotNull { exportName ->
        try {
            instance.export(exportName)
        } catch (@Suppress("SwallowedException") ex: ChicoryException) {
            null
        } catch (@Suppress("SwallowedException", "TooGenericExceptionCaught") ex: NullPointerException) {
            null
        }?.let { exportFunction ->
            exportName to exportFunction
        }
    }.toMap()
    val optional: ReadWriteProperty<Any?, Int?> = object : ReadWriteProperty<Any?, Int?> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int? =
            globals[property.name]?.let { exportFunction ->
                exportFunction.apply()[0].toInt()
            }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int?) {
            checkNotNull(value) { "Can not set global ${property.name} to null" }
            val functionExport = globals[property.name]
            functionExport?.apply(value.toLong())
        }
    }
    val required: ReadWriteProperty<Any?, Int> = object : ReadWriteProperty<Any?, Int> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            val exportFunction = getExportedFunction(property.name)
            return exportFunction.apply()[0].toInt()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            val global = getExportedFunction(property.name)
            global.apply(value.toLong())
        }

        private fun getExportedFunction(name: String): ExportFunction = globals.getOrElse(name) {
            error("Export $name not found")
        }
    }
}
