/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.wasip1

import at.released.weh.bindings.graalvm241.memory.GraalvmWasmHostMemoryAdapter
import at.released.weh.wasi.preview1.function.WasiPreview1HostFunctionHandle
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule
import org.graalvm.wasm.memory.WasmMemory
import org.graalvm.wasm.nodes.WasmRootNode

internal open class BaseWasiWasmNode<H : WasiPreview1HostFunctionHandle>(
    language: WasmLanguage,
    private val module: WasmModule,
    val handle: H,
) : WasmRootNode(language, null, null) {
    override fun getName(): String = "wasm-function:${handle.function.wasmName}"

    override fun module(): WasmModule = module

    fun WasmMemory.toHostMemory() = GraalvmWasmHostMemoryAdapter({ this }, this@BaseWasiWasmNode)
}
