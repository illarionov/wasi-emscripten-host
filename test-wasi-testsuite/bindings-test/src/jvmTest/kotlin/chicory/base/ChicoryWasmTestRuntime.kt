/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")

package at.released.weh.wasi.bindings.test.chicory.base

import at.released.weh.bindings.chicory.exception.ProcExitException
import at.released.weh.bindings.chicory.wasip1.ChicoryWasiPreview1Builder
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import at.released.weh.wasi.bindings.test.runner.WasmTestRuntime
import com.dylibso.chicory.runtime.ByteArrayMemory
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.Instance
import com.dylibso.chicory.runtime.Store
import com.dylibso.chicory.runtime.WasmRuntimeException
import com.dylibso.chicory.wasi.WasiExitException
import com.dylibso.chicory.wasm.Parser
import kotlinx.io.files.Path

class ChicoryWasmTestRuntime(
    private val useByteArrayMemory: Boolean,
) : WasmTestRuntime {
    override val hasOwnStdioTests: Boolean = false

    override fun runTest(
        wasmFile: ByteArray,
        host: EmbedderHost,
        arguments: WasiTestsuiteArguments,
        rootDir: Path,
    ): Int {
        val wasiFunctions: List<HostFunction> = ChicoryWasiPreview1Builder {
            this.host = host
        }.build()
        val store = Store().apply {
            wasiFunctions.forEach { hostFunction -> addFunction(hostFunction) }
        }

        val wasmModule = Parser.parse(wasmFile)

        // Instantiate the WebAssembly module
        val instance = Instance.builder(wasmModule).apply {
            withInitialize(true)
            withStart(false)
            withImportValues(store.toImportValues())
            if (useByteArrayMemory) {
                withMemoryFactory(::ByteArrayMemory)
            }
        }.build()
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

    /**
     * ChicoryWasmTestRuntime configured to use default
     */
    class Factory : WasmTestRuntime.Factory {
        override fun invoke(): WasmTestRuntime = ChicoryWasmTestRuntime(useByteArrayMemory = false)
        override fun close() = Unit
    }

    /**
     * ChicoryWasmTestRuntime configured to use [ByteArrayMemory]
     */
    class ByteArrayMemoryFactory : WasmTestRuntime.Factory {
        override fun invoke(): WasmTestRuntime = ChicoryWasmTestRuntime(useByteArrayMemory = false)
        override fun close() = Unit
    }

    companion object {
        internal fun WasmRuntimeException.findProcExitExceptionCause(): ProcExitException? {
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
    }
}
