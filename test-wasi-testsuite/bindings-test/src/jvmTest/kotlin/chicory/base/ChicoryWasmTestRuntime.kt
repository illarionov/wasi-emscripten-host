/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.chicory.base

import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller
import at.released.weh.bindings.chicory.ProcExitException
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import at.released.weh.wasi.bindings.test.runner.WasmTestRuntime
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.runtime.WasmRuntimeException
import com.dylibso.chicory.wasi.WasiExitException
import com.dylibso.chicory.wasm.Parser
import kotlinx.io.files.Path

object ChicoryWasmTestRuntime : WasmTestRuntime {
    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int {
        val installer = ChicoryHostFunctionInstaller {
            this.host = host
        }
        val wasiFunctions: List<HostFunction> = installer.setupWasiPreview1HostFunctions()
        val store = Store().apply {
            wasiFunctions.forEach { hostFunction -> addFunction(hostFunction) }
        }

        val wasmModule = Parser.parse(wasmFile)

        // Instantiate the WebAssembly module
        val instance = Instance.builder(wasmModule)
            .withInitialize(true)
            .withStart(false)
            .withImportValues(store.toImportValues())
            .build()
        val exitCode = try {
            instance.export("_start").apply()
            0
        } catch (exit: WasiExitException) {
            exit.exitCode()
        } catch (machineException: WasmRuntimeException) {
            machineException.findProcExitExceptionCause()?.exitCode ?: throw machineException
        } catch (prce: ProcExitException) {
            prce.exitCode
        }

        return exitCode
    }

    private fun Throwable.findProcExitExceptionCause(): ProcExitException? {
        val causes: MutableSet<Throwable> = mutableSetOf()
        var ex: Throwable? = this
        while (ex != null) {
            if (ex is ProcExitException) {
                return ex
            }
            if (!causes.add(ex)) {
                break
            }
            ex = ex.cause
        }

        return null
    }

    class Factory : WasmTestRuntime.Factory {
        override fun invoke(): WasmTestRuntime = ChicoryWasmTestRuntime
        override fun close() = Unit
    }
}
