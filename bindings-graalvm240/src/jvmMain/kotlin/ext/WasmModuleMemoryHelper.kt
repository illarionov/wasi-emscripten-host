/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.ext

import at.released.weh.bindings.graalvm240.MemorySource
import at.released.weh.bindings.graalvm240.MemorySource.ExportedExternalMemory
import at.released.weh.bindings.graalvm240.MemorySource.ExportedMemory
import at.released.weh.bindings.graalvm240.MemorySource.ImportedMemory
import at.released.weh.bindings.graalvm240.host.memory.SharedMemoryWaiterListStore
import at.released.weh.bindings.graalvm240.host.memory.WasmMemoryNotifyCallback
import at.released.weh.bindings.graalvm240.host.memory.WasmMemoryWaitCallback
import at.released.weh.common.api.Logger
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmModule

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
            memory.spec.minSizePages,
            memory.spec.maxSizePages,
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
                memory.spec.minSizePages,
                memory.spec.maxSizePages,
                memory.spec.supportMemory64,
                memory.spec.sharedMemory,
                false,
                memory.spec.useUnsafeMemory,
            )
            exportMemory(memoryIndex, memory.memoryName)
        }
        module.addLinkAction { _: WasmContext, instance: WasmInstance ->
            instance.memory(0).apply {
                waitCallback = WasmMemoryWaitCallback(memoryWaiters, logger)
                notifyCallback = WasmMemoryNotifyCallback(memoryWaiters, logger)
            }
        }
    }

    fun setExternalExportedMemory(
        memory: ExportedExternalMemory,
        memoryWaiters: SharedMemoryWaiterListStore,
        logger: Logger,
    ) = memory.sourceContext.withWasmContext { wasmContext ->
        val sharedMemory = wasmContext.memories().memory(memory.sourceMemoryIndex)
        module.symbolTable().apply {
            allocateExternalMemory(0, sharedMemory, false)
            exportMemory(0, memory.exportedName)
        }
        module.addLinkAction { _: WasmContext, instance: WasmInstance ->
            instance.memory(0).apply {
                waitCallback = WasmMemoryWaitCallback(memoryWaiters, logger)
                notifyCallback = WasmMemoryNotifyCallback(memoryWaiters, logger)
            }
        }
    }
}
