/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.poll

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.identity
import arrow.core.left
import arrow.core.mapOrAccumulate
import arrow.core.right
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.PollError
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.op.poll.POLL_PERIOD_NS
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.SUCCESS
import at.released.weh.filesystem.op.poll.Event
import at.released.weh.filesystem.op.poll.Event.ClockEvent
import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.filesystem.op.poll.Subscription
import at.released.weh.filesystem.op.poll.Subscription.ClockSubscription
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionClockId.MONOTONIC
import at.released.weh.filesystem.op.poll.toEvent
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.nativefunc.posixClose
import at.released.weh.host.clock.MonotonicClock
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.posix.POLLIN
import platform.posix.POLLOUT
import platform.posix.pollfd
import kotlin.experimental.and

internal expect fun nativePoll(fds: CValuesRef<pollfd>, nfds: Int, timeout: Int): Int

internal class PosixPollHelper(
    private val monotonicClock: MonotonicClock,
    private val nanosleep: (
        clock: Subscription.SubscriptionClockId,
        nanoseconds: Long,
    ) -> Either<FileSystemErrno, Unit>,
) {
    @Suppress("MagicNumber")
    internal fun pollSubscriptions(
        pollable: List<PollableSubscription>,
        nonPollable: List<NonPollableSubscription>,
        minTimeout: Pair<Long, ClockSubscription>?,
    ): Either<PollError, List<Event>> {
        val nonPollableEvents = nonPollable.collectEvents()
        if (nonPollableEvents.isNotEmpty()) {
            return nonPollableEvents.right()
        }

        val totalWaitNs = minTimeout?.first ?: Long.MAX_VALUE
        val isOneOff = nonPollable.isEmpty()
        val pollPeriodNs = if (isOneOff) {
            totalWaitNs
        } else {
            totalWaitNs.coerceAtMost(POLL_PERIOD_NS)
        }

        // Always use monotonic clock
        val start = monotonicClock.getTimeMarkNanoseconds()
        val endTime = start + totalWaitNs
        var currentTime = start
        var lastSleepResult: FileSystemErrno = SUCCESS

        memScoped {
            val pollFds = allocPollFds(pollable)
            while (currentTime < endTime) {
                val timeoutNs = (endTime - currentTime).coerceAtMost(pollPeriodNs)
                val timeoutMs = ((timeoutNs + 1_000_001) / 1000_000).toInt()

                val pollResult = if (pollable.isNotEmpty()) {
                    nativePoll(pollFds, pollable.size, timeoutMs)
                } else {
                    nanosleep(MONOTONIC, timeoutNs)
                    0
                }
                val receivedEvents: List<Event> = readReceivedEvents(pollable, pollFds, pollResult)
                val totalEvents = receivedEvents + nonPollable.collectEvents()
                if (totalEvents.isNotEmpty() || isOneOff) {
                    return totalEvents.right()
                }
                currentTime = monotonicClock.getTimeMarkNanoseconds()
            }
        }

        return if (minTimeout != null) {
            listOf(ClockEvent(lastSleepResult, minTimeout.second.userdata)).right()
        } else {
            Interrupted("Timeout").left()
        }
    }

    private fun readReceivedEvents(
        subscriptions: List<PollableSubscription>,
        pollFds: CArrayPointer<pollfd>,
        pollResult: Int,
    ): List<Event> {
        if (pollResult <= 0) {
            return emptyList()
        }
        return subscriptions.mapIndexedNotNull { index, subscription: PollableSubscription ->
            val revents = pollFds[index].revents
            val expectedEvents = subscription.subscription.pollEvents()
            if (revents != 0.toShort()) {
                when (subscription.subscription) {
                    is ClockSubscription -> subscription.subscription.toEvent()
                    is FileDescriptorSubscription -> subscription.subscription.toEvent(
                        isHangup = (revents and expectedEvents != expectedEvents),
                    )
                }
            } else {
                null
            }
        }
    }

    private fun List<NonPollableSubscription>.collectEvents(): List<Event> = mapNotNull { (subscription, resource) ->
        resource.pollNonblocking(subscription).fold(
            ifLeft = { error ->
                when (error) {
                    is Again -> null
                    else -> subscription.toEvent(errno = error.errno)
                }
            },
            ifRight = ::identity,
        )
    }

    internal fun MemScope.allocPollFds(subscriptions: List<PollableSubscription>): CArrayPointer<pollfd> {
        return allocArray(subscriptions.size) { index ->
            val sub: PollableSubscription = subscriptions[index]
            fd = sub.fd.fd
            events = sub.subscription.pollEvents()
            revents = 0
        }
    }

    internal fun Subscription.pollEvents(): Short = when (this) {
        is ClockSubscription -> POLLIN.toShort()
        is FileDescriptorSubscription -> this.type.pollEvents()
    }

    internal fun FileDescriptorEventType.pollEvents(): Short = when (this) {
        FileDescriptorEventType.READ -> POLLIN
        FileDescriptorEventType.WRITE -> POLLOUT
    }.toShort()

    fun closeSubscriptions(
        subscriptions: List<PollableSubscription>,
    ): EitherNel<CloseError, Unit> = subscriptions
        .filter(PollableSubscription::needClose)
        .mapOrAccumulate { posixClose(it.fd).bind() }.map { }

    internal data class PollableSubscription(
        val fd: NativeFileFd,
        val needClose: Boolean,
        val subscription: Subscription,
    )

    internal data class NonPollableSubscription(
        val subscription: FileDescriptorSubscription,
        val resource: FdResource,
    )
}
