/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.wasi.bindings.test.chicory.base

import at.released.weh.bindings.chicory.ChicoryHostFunctionInstaller
import at.released.weh.bindings.chicory.ProcExitException
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.bindings.test.runner.RuntimeTestExecutor
import at.released.weh.wasi.bindings.test.runner.WasiTestsuiteArguments
import com.dylibso.chicory.runtime.HostFunction
import com.dylibso.chicory.runtime.HostGlobal
import com.dylibso.chicory.runtime.HostImports
import com.dylibso.chicory.runtime.HostTable
import com.dylibso.chicory.runtime.Module
import com.dylibso.chicory.runtime.exceptions.WASMMachineException
import com.dylibso.chicory.wasi.WasiExitException
import kotlinx.io.files.Path

object ChicoryRuntimeTestExecutor : RuntimeTestExecutor {
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
        val hostImports = HostImports(
            wasiFunctions.toTypedArray(),
            arrayOf<HostGlobal>(),
            emptyArray(),
            arrayOf<HostTable>(),
        )
        val module = Module
            .builder(wasmFile)
            .withHostImports(hostImports)
            .withInitialize(true)
            .withStart(false)
            .build()

        // Instantiate the WebAssembly module
        val instance = module.instantiate()
        val exitCode = try {
            instance.export("_start").apply()
            0
        } catch (exit: WasiExitException) {
            exit.exitCode()
        } catch (machineException: WASMMachineException) {
            machineException.findProcExitExceptionCause()?.exitCode ?: throw machineException
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

    class Factory : RuntimeTestExecutor.Factory {
        override fun invoke(): RuntimeTestExecutor = ChicoryRuntimeTestExecutor
        override fun close() = Unit
    }
}
