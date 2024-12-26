/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("TooGenericExceptionCaught", "InstanceOfCheckForException")

package at.released.weh.wasi.preview1.function

import at.released.weh.filesystem.op.poll.Poll
import at.released.weh.host.EmbedderHost
import at.released.weh.wasi.preview1.WasiPreview1HostFunction
import at.released.weh.wasi.preview1.ext.EventMapper.EVENT_PACKED_SIZE
import at.released.weh.wasi.preview1.ext.EventMapper.fromFilesystemEvent
import at.released.weh.wasi.preview1.ext.EventMapper.packTo
import at.released.weh.wasi.preview1.ext.SubscriptionMapper
import at.released.weh.wasi.preview1.ext.SubscriptionMapper.SUBSCRIPTION_SIZE
import at.released.weh.wasi.preview1.ext.foldToErrno
import at.released.weh.wasi.preview1.type.Errno
import at.released.weh.wasi.preview1.type.Event
import at.released.weh.wasi.preview1.type.Size
import at.released.weh.wasi.preview1.type.SizeType
import at.released.weh.wasi.preview1.type.Subscription
import at.released.weh.wasm.core.IntWasmPtr
import at.released.weh.wasm.core.WasmPtr
import at.released.weh.wasm.core.memory.Memory
import at.released.weh.wasm.core.memory.sinkWithMaxSize
import at.released.weh.wasm.core.memory.sourceWithMaxSize
import kotlinx.io.IOException
import kotlinx.io.buffered
import at.released.weh.filesystem.op.poll.Event as FileSystemEvent

public class PollOneoffFunctionHandle(
    host: EmbedderHost,
) : WasiPreview1HostFunctionHandle(WasiPreview1HostFunction.POLL_ONEOFF, host) {
    public fun execute(
        memory: Memory,
        @IntWasmPtr(Subscription::class) inSubscriptionPtr: WasmPtr,
        @IntWasmPtr(Event::class) outEventsPtr: WasmPtr,
        @SizeType subscriptionCount: Size,
        @IntWasmPtr(Int::class) eventsStoredAddr: WasmPtr,
    ): Errno {
        if (subscriptionCount == 0) {
            return Errno.INVAL
        }
        val subscriptions: List<Subscription> = try {
            memory.sourceWithMaxSize(
                inSubscriptionPtr,
                (SUBSCRIPTION_SIZE * subscriptionCount).toInt(),
            ).buffered().use {
                SubscriptionMapper.readSubscriptions(it, subscriptionCount)
            }
        } catch (ex: Exception) {
            if (ex is IllegalStateException || ex is IllegalArgumentException || ex is IOException) {
                return Errno.INVAL
            } else {
                throw ex
            }
        }

        val fsSubscriptions = subscriptions.map(SubscriptionMapper::toFileSystemSubscription)

        return host.fileSystem.execute(Poll, Poll(fsSubscriptions))
            .onRight { events: List<FileSystemEvent> ->
                val wasiEvents = events.take(subscriptionCount).map(::fromFilesystemEvent)
                memory.writeI32(eventsStoredAddr, wasiEvents.size)
                memory.sinkWithMaxSize(outEventsPtr, wasiEvents.size * EVENT_PACKED_SIZE).buffered().use { sink ->
                    wasiEvents.forEach { event -> event.packTo(sink) }
                }
            }
            .foldToErrno()
    }
}
