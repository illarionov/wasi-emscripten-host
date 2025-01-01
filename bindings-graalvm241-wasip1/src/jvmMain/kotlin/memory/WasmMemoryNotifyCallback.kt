/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.memory

import at.released.weh.bindings.graalvm241.ext.getArgAsInt
import at.released.weh.bindings.graalvm241.ext.getArgAsLong
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import at.released.weh.common.api.Logger
import com.oracle.truffle.api.interop.InteropLibrary
import com.oracle.truffle.api.interop.TruffleObject
import com.oracle.truffle.api.library.ExportLibrary
import com.oracle.truffle.api.library.ExportMessage

/**
 * Сallback to be called from the GraalVM Wasm runtime when atomic_notify Wasm function is executed.
 *
 * @see <a
 *   href="https://github.com/WebAssembly/threads/blob/main/proposals/threads/Overview.md#wait-and-notify-operators"
 * >WebAssembly wait and notify operators</a>
 *
 */
@InternalWasiEmscriptenHostApi
@ExportLibrary(InteropLibrary::class)
public class WasmMemoryNotifyCallback(
    private val waitersStore: SharedMemoryWaiterListStore,
    logger: Logger,
) : TruffleObject {
    private val logger = logger.withTag("WasmMemoryNotifyCallback")

    @Suppress("FunctionOnlyReturningConstant")
    @ExportMessage
    public fun isExecutable(): Boolean {
        return true
    }

    @ExportMessage
    public fun execute(arguments: Array<Any>): Any {
        val addr = arguments.getArgAsLong(0)
        val count = arguments.getArgAsInt(1)
        logger.v { "atomic_notify(addr = 0x${addr.toString(16)}, count = $count)" }

        val waiterListRecord = waitersStore.getListForIndex((addr * 4).toInt())
        return waiterListRecord.notifyWaiters(count)
    }
}
