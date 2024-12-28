/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.internal.op.poll

import arrow.core.Either
import arrow.core.identity
import arrow.core.left
import arrow.core.right
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.NonblockingPollError
import at.released.weh.filesystem.error.PollError
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.INVAL
import at.released.weh.filesystem.model.FileSystemErrno.SUCCESS
import at.released.weh.filesystem.op.poll.Event
import at.released.weh.filesystem.op.poll.Event.ClockEvent
import at.released.weh.filesystem.op.poll.Subscription
import at.released.weh.filesystem.op.poll.Subscription.ClockSubscription
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionClockId.MONOTONIC
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionClockId.REALTIME
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionTimeout
import at.released.weh.host.clock.MonotonicClock

internal const val POLL_PERIOD_NS = 100_000_000L

internal class PollHelper(
    private val fdsResourceProvider: (FileDescriptor) -> FdResource?,
    private val monotonicClock: MonotonicClock,
    private val nanosleep: (
        clock: Subscription.SubscriptionClockId,
        nanoseconds: Long,
    ) -> Either<FileSystemErrno, Unit>,
) {
    internal fun groupSubscriptions(
        subscriptions: List<Subscription>,
    ): EventGroups {
        val fdsToBlock: MutableList<Pair<FileDescriptorSubscription, FdResource>> = ArrayList(subscriptions.size)
        val clockSubscriptions: MutableList<ClockSubscription> = ArrayList(1)
        val events: MutableList<Event> = ArrayList(subscriptions.size)

        subscriptions.forEach { subscription: Subscription ->
            when (subscription) {
                is ClockSubscription -> if ((subscription.clock == REALTIME || subscription.clock == MONOTONIC) &&
                    subscription.timeout is SubscriptionTimeout.Relative
                ) {
                    clockSubscriptions.add(subscription)
                } else {
                    events.add(ClockEvent(INVAL, subscription.userdata))
                }

                is FileDescriptorSubscription -> {
                    val fd = fdsResourceProvider(subscription.fileDescriptor)
                    if (fd != null) {
                        fd.pollNonblocking(subscription).fold(
                            ifLeft = { error: NonblockingPollError ->
                                when (error) {
                                    is Again -> fdsToBlock.add(subscription to fd)
                                    else -> events.add(subscription.toErrorEvent(error.errno))
                                }
                            },
                            ifRight = { event -> events.add(event) },
                        )
                    } else {
                        events.add(subscription.toErrorEvent())
                    }
                }
            }
        }
        return EventGroups(fdsToBlock, clockSubscriptions, events)
    }

    internal fun pollSubscriptions(
        fdsToWait: List<Pair<FileDescriptorSubscription, FdResource>>,
        clockSubscriptions: List<ClockSubscription>,
    ): Either<PollError, List<Event>> {
        val minTimeoutSubscription: ClockSubscription? = clockSubscriptions.minByOrNull { it.timeout.timeoutNs }
        val waitNs = minTimeoutSubscription?.timeout?.timeoutNs ?: Long.MAX_VALUE

        // Always use monotonic clock
        val start = monotonicClock.getTimeMarkNanoseconds()
        val end = start + waitNs
        var current = start
        var lastSleepResult: FileSystemErrno = SUCCESS
        while (current < end) {
            val timeout = (end - current).coerceAtMost(POLL_PERIOD_NS)
            val result = nanosleep(MONOTONIC, timeout).fold(::identity) { SUCCESS }
            if (result != SUCCESS) {
                break
            }

            val events = fdsToWait.mapNotNull { (subscription, resource) ->
                resource.pollNonblocking(subscription).fold(
                    ifLeft = { error ->
                        when (error) {
                            is Again -> null
                            else -> subscription.toErrorEvent(error.errno)
                        }
                    },
                    ifRight = ::identity,
                )
            }

            if (events.isNotEmpty()) {
                return events.right()
            }
            current = monotonicClock.getTimeMarkNanoseconds()
        }

        return if (minTimeoutSubscription != null) {
            listOf(ClockEvent(lastSleepResult, minTimeoutSubscription.userdata)).right()
        } else {
            Interrupted("Timeout").left()
        }
    }

    internal fun waitNearestTimer(
        clockSubscriptions: List<ClockSubscription>,
    ): Either<PollError, List<Event>> {
        val minTimeoutSubscription: ClockSubscription = clockSubscriptions.minBy { it.timeout.timeoutNs }
        val waitNs = minTimeoutSubscription.timeout.timeoutNs
        val errno = nanosleep(minTimeoutSubscription.clock, waitNs).fold(::identity) { SUCCESS }
        return listOf(ClockEvent(errno, minTimeoutSubscription.userdata)).right()
    }

    internal fun FileDescriptorSubscription.toErrorEvent(
        errno: FileSystemErrno = BADF,
    ) = Event.FileDescriptorEvent(
        errno = errno,
        userdata = userdata,
        fileDescriptor = fileDescriptor,
        type = type,
        bytesAvailable = 0,
        isHangup = false,
    )

    internal class EventGroups(
        val fdsToBlock: List<Pair<FileDescriptorSubscription, FdResource>>,
        val clockSubscriptions: List<ClockSubscription>,
        val events: List<Event>,
    )
}
