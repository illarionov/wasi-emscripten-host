/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT")

package at.released.weh.host.include

import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr

/**
 * Internal Pthread struct
 *
 * [pthread_impl.h](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/libc/musl/src/internal/pthread_impl.h)
 */
public data class StructPthread(
    @IntWasmPtr(StructPthread::class)
    val self: WasmPtr,

    // uintptr_t *dtv;

    @IntWasmPtr(StructPthread::class)
    val prev: WasmPtr,

    @IntWasmPtr(StructPthread::class)
    val next: WasmPtr,

    val sysinfo: uintptr_t,
    // canary_pad
    val canary: uintptr_t,
    // Part 2 -- implementation details, non-ABI.
    // …
    /* Part 3 -- the positions of these fields relative to
     * the end of the structure is external and internal ABI. */
    // …

    //    Emscripten specifics
    // thread_profiler_block * _Atomic profilerBlock;

    /**
     * The TLS base to use the main module TLS data. Secondary modules
     * still require dynamic allocation.
     */
    @IntWasmPtr
    val tlsBase: WasmPtr,

    /**
     * The lowest level of the proxying system. Other threads can enqueue
     * messages on the mailbox and notify this thread to asynchronously
     * process them once it returns to its event loop. When this thread is
     * shut down, the mailbox is closed (see below) to prevent further
     * messages from being enqueued and all the remaining queued messages
     * are dequeued and their shutdown handlers are executed. This allows
     * other threads waiting for their messages to be processed to be
     * notified that their messages will not be processed after all.
     */
    @IntWasmPtr
    val mailbox: WasmPtr,

    /**
     * To ensure that no other thread is concurrently enqueueing a message
     * when this thread shuts down, maintain an atomic refcount. Enqueueing
     * threads atomically increment the count from a nonzero number to
     * acquire the mailbox and decrement the count when they finish. When
     * this thread shuts down it will atomically decrement the count and
     * wait until it reaches 0, at which point the mailbox is considered
     * closed and no further messages will be enqueued.
     * _Atomic int mailbox_refcount;
     */
    val mailboxRefcount: Int,

    /**
     * Whether the thread has executed an `Atomics.waitAsync` on this
     * pthread struct and can be notified of new mailbox messages via
     * `Atomics.notify`. Otherwise, such as when the environment does not
     * implement `Atomics.waitAsync` or when the thread has not had a chance
     * to initialize itself yet, the notification has to fall back to the
     * postMessage path. Once this becomes true, it remains true so we never
     * fall back to postMessage unnecessarily.
     */
    val waitingAsync: Int,

    /**
     * When dynamic linking is enabled, threads use this to facilitate the
     * synchronization of loaded code between threads.
     * See [emscripten_futex_wait.c](https://github.com/emscripten-core/emscripten/blob/3.1.61/system/lib/pthread/emscripten_futex_wait.c)
     */
    val sleeping: Byte,
) {
    public companion object {
        public const val STRUCT_PTHREAD_STACK_HIGH_OFFSET: Int = 52
        public const val STRUCT_PTHREAD_STACK_SZIE_OFFSET: Int = 56
    }
}
