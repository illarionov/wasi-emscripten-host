/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm240.host

import at.released.weh.common.api.Logger
import at.released.weh.host.base.memory.Memory
import at.released.weh.host.emscripten.export.EmscriptenMainExports
import at.released.weh.host.emscripten.export.EmscriptenRuntime
import at.released.weh.host.emscripten.export.pthread.EmscriptenPthread
import at.released.weh.host.emscripten.export.pthread.EmscriptenPthreadInternal
import at.released.weh.host.emscripten.export.stack.EmscriptenStackExports
import at.released.weh.host.include.StructPthread
import at.released.weh.host.include.StructPthread.Companion.STRUCT_PTHREAD_STACK_HIGH_OFFSET
import at.released.weh.host.include.StructPthread.Companion.STRUCT_PTHREAD_STACK_SZIE_OFFSET
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

public class GraalvmEmscriptenRuntime(
    mainExports: EmscriptenMainExports,
    stackExports: EmscriptenStackExports,
    memory: Memory,
    private val emscriptenPthread: EmscriptenPthread?,
    private val emscriptenPthreadInternal: EmscriptenPthreadInternal?,
    rootLogger: Logger,
) : EmscriptenRuntime(mainExports, stackExports, memory, rootLogger) {
    public override val isMultiThread: Boolean get() = emscriptenPthreadInternal != null

    public override fun initMainThread(): Unit = if (isMultiThread) {
        initMultithreadedMainThread()
    } else {
        initSingleThreadedMainThread()
    }

    private fun initMultithreadedMainThread() {
        stack.stackCheckInit(memory)
        stack.setStackLimits()
        mainExports.__wasm_call_ctors.executeVoid()
        stack.checkStackCookie(memory)
    }

    public fun initWorkerThread(
        @IntWasmPtr(StructPthread::class) threadPtr: WasmPtr,
    ) {
        val pthreadInternal = checkNotNullInMultithreaded(emscriptenPthreadInternal)

        pthreadInternal.emscriptenThreadInit(
            threadPtr,
            isMain = false,
            isRuntime = false,
            canBlock = true,
            defaultStackSize = 0,
            startProfiling = false,
        )
        establishStackSpace()
        pthreadInternal.emscriptenTlsInit()
    }

    private fun establishStackSpace() {
        val pthread = checkNotNullInMultithreaded(emscriptenPthread)

        @IntWasmPtr(StructPthread::class)
        val pthreadPtr: WasmPtr = pthread.pthreadSelf().toInt()
        val stackHigh: Int = memory.readI32(pthreadPtr + STRUCT_PTHREAD_STACK_HIGH_OFFSET)
        val stackSize: Int = memory.readI32(pthreadPtr + STRUCT_PTHREAD_STACK_SZIE_OFFSET)

        val stackLow = stackHigh - stackSize
        check(stackHigh != 0 && stackLow != 0)
        check(stackHigh > stackLow) { "stackHigh must be higher then stackLow" }
        check(stackLow != 0)

        stack.emscriptenStackSetLimits(stackHigh, stackLow)
        stack.setStackLimits()
        stack.emscriptenStackRestore(stackHigh)
        stack.writeStackCookie(memory)
    }

    private fun <T : Any> checkNotNullInMultithreaded(value: T?): T = checkNotNull(value) {
        "This function should not be called in a single threaded environment"
    }

    public companion object {
        public fun multithreadedRuntime(
            mainExports: EmscriptenMainExports,
            stackExports: EmscriptenStackExports,
            memory: Memory,
            emscriptenPthread: EmscriptenPthread,
            emscriptenPthreadInternal: EmscriptenPthreadInternal,
            logger: Logger,
        ): GraalvmEmscriptenRuntime {
            return GraalvmEmscriptenRuntime(
                mainExports = mainExports,
                stackExports = stackExports,
                emscriptenPthread = emscriptenPthread,
                emscriptenPthreadInternal = emscriptenPthreadInternal,
                memory = memory,
                rootLogger = logger,
            )
        }
    }
}
