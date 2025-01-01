/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.memory

import at.released.weh.bindings.graalvm241.ext.getArgAsLong
import at.released.weh.bindings.graalvm241.memory.SharedMemoryWaiterListStore.AtomicsWaitResult
import at.released.weh.bindings.graalvm241.memory.SharedMemoryWaiterListStore.WaiterListRecord
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.common.api.Logger
import com.oracle.truffle.api.interop.InteropLibrary
import com.oracle.truffle.api.interop.TruffleObject
import com.oracle.truffle.api.library.ExportLibrary
import com.oracle.truffle.api.library.ExportMessage
import org.graalvm.wasm.WasmArguments
import org.graalvm.wasm.memory.WasmMemory
import java.lang.invoke.VarHandle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Сallback to be called from the GraalVM Wasm runtime when atomic_wait64 / atomic_wait32 Wasm functions are executed.
 *
 * Suspends the calling thread if the memory pointed to by addr is equal to expectedValue until a notification
 * is received from [WasmMemoryNotifyCallback], or until the timeout expires.
 *
 * @see <a
 *   href="https://github.com/WebAssembly/threads/blob/main/proposals/threads/Overview.md#wait-and-notify-operators"
 * >WebAssembly wait and notify operators</a>
 *
 */
@InternalWasiEmscriptenHostApi
@ExportLibrary(InteropLibrary::class)
public class WasmMemoryWaitCallback(
    private val waitersStore: SharedMemoryWaiterListStore,
    logger: Logger,
) : TruffleObject {
    private val logger = logger.withTag("WasmMemoryWaitCallback")

    @Suppress("FunctionOnlyReturningConstant")
    @ExportMessage
    public fun isExecutable(): Boolean = true

    @ExportMessage
    public fun execute(arguments: Array<Any>): Any {
        val wasmMemory = arguments[0] as WasmMemory
        val addr = arguments.getArgAsLong(0).toInt()
        val expectedValue = arguments.getArgAsLong(1)
        val timeout = arguments.getArgAsLong(2)
        val is64 = WasmArguments.getArgument(arguments, 3) as Boolean
        logger.v {
            "execute(): ${"0x" + addr.toString(16)} ${"0x" + expectedValue.toString(16)} " +
                    "$timeout ms, is64: $is64"
        }

        val list: WaiterListRecord = waitersStore.getListForIndex(
            if (is64) addr * 8 else addr * 4,
        )
        list.withCriticalSection {
            if (is64) {
                val longValue = wasmMemory.atomic_load_i64(null, addr * 8L)
                acquireFence()
                if (longValue != expectedValue) {
                    logger.v { "AtomicsWaitResult.NOT_EQUAL" }
                    return AtomicsWaitResult.NOT_EQUAL.id
                }
            } else {
                val intValue = wasmMemory.atomic_load_i32(null, addr * 4L)
                acquireFence()
                if (intValue.toLong() != expectedValue) {
                    logger.v { "AtomicsWaitResult.NOT_EQUAL" }
                    return AtomicsWaitResult.NOT_EQUAL.id
                }
            }
            logger.v { "list.suspend()" }
            return list.suspend(if (timeout < 0) Duration.INFINITE else timeout.milliseconds)
        }
    }

    @Suppress("NewApi")
    private fun acquireFence() {
        // Min API 33
        VarHandle.acquireFence()
    }

    @InternalWasiEmscriptenHostApi
    public class WasmInterruptedException(ie: Throwable) : RuntimeException(ie)
}
