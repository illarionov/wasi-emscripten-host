/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.ext

import at.released.weh.bindings.graalvm241.MemorySource
import at.released.weh.bindings.graalvm241.MemorySource.ExportedExternalMemory
import at.released.weh.bindings.graalvm241.MemorySource.ExportedMemory
import at.released.weh.bindings.graalvm241.MemorySource.ImportedMemory
import at.released.weh.bindings.graalvm241.host.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm241.host.memory.WasmMemoryNotifyCallback
import at.released.weh.bindings.graalvm241.host.memory.WasmMemoryWaitCallback
import at.released.weh.common.api.Logger
import org.graalvm.wasm.ImportDescriptor
import org.graalvm.wasm.SymbolTable
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory
import java.util.function.Function

internal class WasmModuleMemoryHelper(
    private val module: WasmModule,
) {
    fun setupMemory(
        source: MemorySource,
        memoryWaiters: SharedMemoryWaiterListStore,
        logger: Logger,
    ) = when (source) {
        is ImportedMemory -> setImportedMemory(source)
        is ExportedMemory -> setExportedMemory(source, memoryWaiters, logger)
        is ExportedExternalMemory -> setExternalExportedMemory(source, memoryWaiters, logger)
    }

    fun setImportedMemory(
        memory: ImportedMemory,
    ) {
        val index = module.memoryCount()
        module.importMemory(
            memory.moduleName,
            memory.memoryName,
            index,
            memory.spec.minSize.count,
            memory.spec.maxSize.count,
            memory.spec.supportMemory64,
            memory.spec.sharedMemory,
            false,
            memory.spec.useUnsafeMemory,
        )
    }

    fun setExportedMemory(
        memory: MemorySource.ExportedMemory,
        memoryWaiters: SharedMemoryWaiterListStore,
        logger: Logger,
    ) {
        module.symbolTable().apply {
            val memoryIndex = memoryCount()
            allocateMemory(
                memoryIndex,
                memory.spec.minSize.count,
                memory.spec.maxSize.count,
                memory.spec.supportMemory64,
                memory.spec.sharedMemory,
                false,
                memory.spec.useUnsafeMemory,
            )
            exportMemory(memoryIndex, memory.memoryName)
        }
        module.addLinkAction { context: WasmContext, _: WasmInstance, _: Function<ImportDescriptor, Any>? ->
            context.memWaitCallback = WasmMemoryWaitCallback(memoryWaiters, logger)
            context.memNotifyCallback = WasmMemoryNotifyCallback(memoryWaiters, logger)
        }
    }

    fun setExternalExportedMemory(
        memory: ExportedExternalMemory,
        memoryWaiters: SharedMemoryWaiterListStore,
        logger: Logger,
    ) = memory.sourceContext.withWasmContext { wasmContext ->
        val sharedMemory: WasmMemory = wasmContext.memories().memory(memory.sourceMemoryIndex)

        module.symbolTable().apply {
            val multiMemory = false
            val memoryIndex = 0
            addMemoryWithReflection(
                index = memoryIndex,
                minSize = sharedMemory.declaredMinSize(),
                maxSize = sharedMemory.declaredMaxSize(),
                maxAllowedSize = sharedMemory.maxAllowedSize(),
                indexType64 = sharedMemory.hasIndexType64(),
                shared = sharedMemory.isShared,
                multiMemory = multiMemory,
                useUnsafeMemory = sharedMemory.isUnsafe,
            )
            exportMemory(memoryIndex, memory.exportedName)
        }
        module.addLinkAction { context: WasmContext, instance: WasmInstance, _: Function<ImportDescriptor, Any>? ->
            val index = context.memories().registerExternal(sharedMemory)
            instance.setMemory(index, sharedMemory)
            context.memWaitCallback = WasmMemoryWaitCallback(memoryWaiters, logger)
            context.memNotifyCallback = WasmMemoryNotifyCallback(memoryWaiters, logger)
        }
    }

    private fun SymbolTable.addMemoryWithReflection(
        index: Int,
        minSize: Long,
        maxSize: Long,
        maxAllowedSize: Long,
        indexType64: Boolean,
        shared: Boolean,
        multiMemory: Boolean,
        useUnsafeMemory: Boolean,
    ) {
        val addMemoryMethod = SymbolTable::class.java.getDeclaredMethod(
            "addMemory",
            Int::class.java,
            Long::class.java,
            Long::class.java,
            Long::class.java,
            Boolean::class.java,
            Boolean::class.java,
            Boolean::class.java,
            Boolean::class.java,
        ).apply {
            isAccessible = true
        }
        addMemoryMethod.invoke(
            this,
            index,
            minSize,
            maxSize,
            maxAllowedSize,
            indexType64,
            shared,
            multiMemory,
            useUnsafeMemory,
        )
    }
}
