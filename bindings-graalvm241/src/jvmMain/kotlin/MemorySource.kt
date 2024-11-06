/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241

import at.released.weh.wasm.core.memory.WASM_MEMORY_32_MAX_PAGES
import org.graalvm.polyglot.Context

public sealed class MemorySource {
    /**
     * The memory will be created in the module and exported as [memoryName].
     */
    public class ExportedMemory(
        public val memoryName: String = "memory",
        public val spec: MemorySpec = MemorySpec.Builder().build(),
    ) : MemorySource()

    /**
     * The memory is imported from the module [moduleName], where it is exported as [memoryName].
     * The imported memory's limits are defined by the [spec].
     *
     * To import memory from the main module, set [moduleName] to "main" (this can be used to instantiate the
     * WASI Snapshot Preview 1 module). In WebAssembly files compiled with Emscripten, memory is typically provided
     * by a module named "env".
     */
    public class ImportedMemory(
        public val moduleName: String = "main",
        public val memoryName: String = "memory",
        public val spec: MemorySpec = MemorySpec.Builder()
            .setMaxSize(WASM_MEMORY_32_MAX_PAGES)
            .build(),
    ) : MemorySource()

    /**
     * Shared memory from [sourceContext] with index [sourceMemoryIndex] will be installed in the module
     * and exported as [exportedMemoryName].
     *
     * Experimental hack, do not use.
     * It is intended for using common shared memory across different GraalVM contexts, each created for its own thread.
     */
    public class ExportedExternalMemory(
        public val sourceContext: Context,
        public val sourceMemoryIndex: Int = 0,
        public val exportedName: String = "memory",
    ) : MemorySource()
}
