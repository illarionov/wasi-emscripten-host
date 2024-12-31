/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.chicory.memory

import at.released.weh.filesystem.FileSystem
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryReader
import at.released.weh.wasi.preview1.memory.DefaultWasiMemoryWriter
import at.released.weh.wasi.preview1.memory.WasiMemoryReader
import at.released.weh.wasi.preview1.memory.WasiMemoryWriter
import at.released.weh.wasm.core.memory.Memory
import com.dylibso.chicory.runtime.Instance

internal fun chicoryWasiMemoryReaderProvider(
    chicoryMemoryProvider: ChicoryMemoryProvider,
    fileSystem: FileSystem,
): (Instance) -> WasiMemoryReader = ChicoryWasiMemoryProvider(chicoryMemoryProvider, fileSystem) { memory, fs ->
    val optimizedReader = if (memory is ChicoryMemoryAdapter) {
        ChicoryWasiMemoryReader.tryCreate(memory.wasmMemory, fs)
    } else {
        null
    }
    optimizedReader ?: DefaultWasiMemoryReader(memory, fs)
}

internal fun chicoryWasiMemoryWriterProvider(
    chicoryMemoryProvider: ChicoryMemoryProvider,
    fileSystem: FileSystem,
): (Instance) -> WasiMemoryWriter = ChicoryWasiMemoryProvider(chicoryMemoryProvider, fileSystem) { memory, fs ->
    val optimizedWriter = if (memory is ChicoryMemoryAdapter) {
        ChicoryWasiMemoryWriter.tryCreate(memory.wasmMemory, fs)
    } else {
        null
    }
    optimizedWriter ?: DefaultWasiMemoryWriter(memory, fs)
}

private class ChicoryWasiMemoryProvider<R : Any>(
    private val chicoryMemoryProvider: ChicoryMemoryProvider,
    private val fileSystem: FileSystem,
    private val instanceFactory: (memory: Memory, fileSystem: FileSystem) -> R,
) : (Instance) -> R {
    private var moduleInstance: Instance? = null
    private var cachedInstance: R? = null

    override fun invoke(instance: Instance): R {
        val cached = this.cachedInstance
        val moduleInstance = this.moduleInstance
        if (cached != null && moduleInstance == instance) {
            return cached
        }
        val memoryAdapter = chicoryMemoryProvider.get(instance)
        val reader = instanceFactory(memoryAdapter, fileSystem)
        this.cachedInstance = reader
        this.moduleInstance = instance
        return reader
    }
}
