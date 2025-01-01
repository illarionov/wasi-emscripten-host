/*
 * Copyright 2024-2025, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.bindings.graalvm241.memory

import at.released.weh.bindings.graalvm241.memory.WasmMemoryWaitCallback.WasmInterruptedException
import at.released.weh.common.api.InternalWasiEmscriptenHostApi
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

@InternalWasiEmscriptenHostApi
public class SharedMemoryWaiterListStore {
    private val waiterLists: ConcurrentHashMap<Int, WaiterListRecord> = ConcurrentHashMap()

    public fun getListForIndex(index: Int): WaiterListRecord {
        return waiterLists.computeIfAbsent(index) { _ -> WaiterListRecord() }
    }

    @InternalWasiEmscriptenHostApi
    public class WaiterListRecord {
        @PublishedApi
        internal val waiters: ConcurrentLinkedQueue<WaiterRecord> = ConcurrentLinkedQueue()

        @PublishedApi
        internal val criticalSession: ReentrantLock = ReentrantLock()

        @PublishedApi
        internal val waitCondition: Condition = criticalSession.newCondition()

        public inline fun <R : Any> withCriticalSection(block: () -> R): R {
            check(!criticalSession.isHeldByCurrentThread)
            return criticalSession.withLock(block)
        }

        public fun suspend(
            timeout: Duration,
        ): AtomicsWaitResult {
            check(criticalSession.isHeldByCurrentThread)

            val waiterRecord = WaiterRecord()
            waiters.add(waiterRecord)

            val finiteTimeout = timeout.isFinite()
            var nanosRemaining = if (finiteTimeout) {
                timeout.inWholeNanoseconds
            } else {
                0L
            }

            try {
                while (true) {
                    if (waiterRecord.notified) {
                        return AtomicsWaitResult.OK
                    }
                    if (finiteTimeout) {
                        nanosRemaining = waitCondition.awaitNanos(nanosRemaining)
                        if (nanosRemaining <= 0) {
                            return AtomicsWaitResult.TIMED_OUT
                        }
                    } else {
                        waitCondition.await()
                    }
                }
            } catch (ie: InterruptedException) {
                throw WasmInterruptedException(ie)
            } finally {
                waiters.remove(waiterRecord)
            }
        }

        public fun notifyWaiters(count: Int): Int = withCriticalSection {
            check(criticalSession.isHeldByCurrentThread)
            val waiters = waiters.remove(count)
            waiters.forEach {
                it.notified = true
            }
            if (waiters.isNotEmpty()) {
                waitCondition.signalAll()
            }
            return waiters.size
        }

        private fun <T> ConcurrentLinkedQueue<T>.remove(count: Int): List<T> {
            val maxCount = if (count < 0) {
                this.size
            } else {
                count
            }
            return MutableList(maxCount.coerceAtMost(this.size)) { this.poll() }
        }
    }

    @InternalWasiEmscriptenHostApi
    public class WaiterRecord(
        public var notified: Boolean = false,
    )

    @InternalWasiEmscriptenHostApi
    public enum class AtomicsWaitResult(public val id: Int) {
        OK(0),
        NOT_EQUAL(1),
        TIMED_OUT(2),
    }
}
