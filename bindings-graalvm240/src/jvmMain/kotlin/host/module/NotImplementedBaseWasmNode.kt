/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host.module

import at.released.weh.host.EmbedderHost
import at.released.weh.host.base.function.HostFunction
import at.released.weh.host.base.function.HostFunctionHandle
import com.oracle.truffle.api.frame.VirtualFrame
import org.graalvm.wasm.WasmContext
import org.graalvm.wasm.WasmInstance
import org.graalvm.wasm.WasmLanguage
import org.graalvm.wasm.WasmModule

internal fun notImplementedFunctionNodeFactory(function: HostFunction): NodeFactory = { language, module, host ->
    NotImplementedBaseWasmNode(language, module, host, function)
}

private class NotImplementedBaseWasmNode(
    language: WasmLanguage,
    module: WasmModule,
    host: EmbedderHost,
    hostFunction: HostFunction,
) : BaseWasmNode<NotImplementedFunctionHandle>(language, module, NotImplementedFunctionHandle(host, hostFunction)) {
    override fun executeWithContext(frame: VirtualFrame, context: WasmContext, instance: WasmInstance) {
        handle.execute()
    }
}

internal class NotImplementedFunctionHandle(
    host: EmbedderHost,
    private val hostFunction: HostFunction,
) : HostFunctionHandle(hostFunction, host) {
    fun execute() {
        error("`${hostFunction.wasmName}`not implemented")
    }
}
