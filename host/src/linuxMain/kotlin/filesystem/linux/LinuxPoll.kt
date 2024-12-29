/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.identity
import arrow.core.left
import arrow.core.mapOrAccumulate
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.Again
import at.released.weh.filesystem.error.CloseError
import at.released.weh.filesystem.error.Interrupted
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.PollError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.fdresource.FdResource
import at.released.weh.filesystem.internal.op.poll.POLL_PERIOD_NS
import at.released.weh.filesystem.internal.op.poll.getMinimalTimeoutOrNull
import at.released.weh.filesystem.internal.op.poll.toErrorEvent
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.LinuxSettimeError
import at.released.weh.filesystem.linux.native.LinuxTimerfdCreateError
import at.released.weh.filesystem.linux.native.linuxTimerfdCreate
import at.released.weh.filesystem.linux.native.linuxTimerfdSetTime
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.NOTSUP
import at.released.weh.filesystem.model.FileSystemErrno.SUCCESS
import at.released.weh.filesystem.op.poll.Event
import at.released.weh.filesystem.op.poll.Event.ClockEvent
import at.released.weh.filesystem.op.poll.Event.FileDescriptorEvent
import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.filesystem.op.poll.Poll
import at.released.weh.filesystem.op.poll.Subscription
import at.released.weh.filesystem.op.poll.Subscription.ClockSubscription
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionClockId
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionTimeout
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.nativefunc.posixClose
import at.released.weh.filesystem.posix.op.poll.posixClockId
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.linux.clock.LinuxClock
import at.released.weh.host.linux.clock.LinuxMonotonicClock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.CLOCK_MONOTONIC
import platform.posix.CLOCK_REALTIME
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.ENOTSUP
import platform.posix.POLLIN
import platform.posix.POLLOUT
import platform.posix.clock_nanosleep
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.timespec
import kotlin.experimental.and

internal class LinuxPoll(
    private val fsState: LinuxFileSystemState,
    private val realtimeClock: Clock = LinuxClock,
    private val monotonicClock: MonotonicClock = LinuxMonotonicClock,
) : FileSystemOperationHandler<Poll, PollError, List<Event>> {
    override fun invoke(input: Poll): Either<PollError, List<Event>> = either {
        val (pollable, nonPollable, events) = fsState.fdsLock.withLock {
            groupSubscriptions(input.subscriptions)
        }

        return try {
            if (events.isNotEmpty()) {
                return events.right()
            }
            if (pollable.isEmpty() && nonPollable.isEmpty()) {
                return InvalidArgument("Subscription list is empty").left()
            }
            val timeoutSubscription = getMinimalTimeoutOrNull(
                input.subscriptions.filterIsInstance<ClockSubscription>(),
                realtimeClock,
                monotonicClock,
            )
            pollSubscriptions(pollable, nonPollable, timeoutSubscription)
        } finally {
            closeSubscriptions(pollable)
        }
    }

    private fun groupSubscriptions(
        subscriptions: List<Subscription>,
    ): PosixEventGroups {
        val pollable: MutableList<PollableSubscription> = ArrayList(subscriptions.size)
        val nonPollable: MutableList<NonPollableSubscription> = mutableListOf()
        val events: MutableList<Event> = ArrayList(subscriptions.size)

        subscriptions.forEach { subscription: Subscription ->
            when (subscription) {
                is ClockSubscription -> createTimerSubscription(subscription)
                    .fold(
                        ifLeft = { error -> events.add(ClockEvent(error, subscription.userdata)) },
                        ifRight = { pollable.add(it) },
                    )

                is FileDescriptorSubscription -> {
                    val channel = fsState.get(subscription.fileDescriptor)
                    if (channel != null) {
                        // TODO: detect subscriptions that can be polled using native poll()
                        nonPollable.add(NonPollableSubscription(subscription, channel))
                    } else {
                        events.add(subscription.toErrorEvent())
                    }
                }
            }
        }
        return PosixEventGroups(pollable, nonPollable, events)
    }

    @Suppress("LongMethod", "MagicNumber")
    private fun pollSubscriptions(
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
            val pollFdsSize = pollable.size
            val pollFds: CArrayPointer<pollfd> = allocArray(pollFdsSize) { index ->
                val pollable: PollableSubscription = pollable[index]
                fd = pollable.fd.fd
                events = pollable.subscription.pollEvents()
                revents = 0
            }

            while (currentTime < endTime) {
                val timeoutNs = (endTime - currentTime).coerceAtMost(pollPeriodNs)
                val timeoutMs = ((timeoutNs + 1_000_001) / 1000_000).toInt() // TODO: do not round?

                val pollResult = if (pollFdsSize != 0) {
                    poll(pollFds, pollFdsSize.toULong(), timeoutMs)
                } else {
                    nanosleep(SubscriptionClockId.MONOTONIC, timeoutNs)
                    0
                }
                val receivedEvents: List<FileDescriptorEvent> = if (pollResult > 0) {
                    nonPollable.mapIndexedNotNull { index, nonPollableSubscription: NonPollableSubscription ->
                        val revents = pollFds[index].revents
                        val expectedEvents = nonPollableSubscription.subscription.pollEvents()
                        if (revents != 0.toShort()) {
                            // TODO check event type?
                            FileDescriptorEvent(
                                errno = SUCCESS,
                                userdata = nonPollableSubscription.subscription.userdata,
                                fileDescriptor = nonPollableSubscription.subscription.fileDescriptor,
                                type = nonPollableSubscription.subscription.type,
                                bytesAvailable = 0,
                                isHangup = (revents and expectedEvents != expectedEvents),
                            )
                        } else {
                            null
                        }
                    }
                } else {
                    emptyList()
                }
                val nonPollableEvents = nonPollable.collectEvents()
                val totalEvents = nonPollableEvents + receivedEvents
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
}

private fun Subscription.pollEvents(): Short = when (this) {
    is ClockSubscription -> POLLIN.toShort()
    is FileDescriptorSubscription -> this.type.pollEvents()
}

private fun FileDescriptorEventType.pollEvents(): Short = when (this) {
    FileDescriptorEventType.READ -> POLLIN
    FileDescriptorEventType.WRITE -> POLLOUT
}.toShort()

private fun List<NonPollableSubscription>.collectEvents(): List<Event> = mapNotNull { (subscription, resource) ->
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

private fun closeSubscriptions(
    subscriptions: List<PollableSubscription>,
): EitherNel<CloseError, Unit> = subscriptions
    .filter(PollableSubscription::needClose)
    .mapOrAccumulate { posixClose(it.fd).bind() }.map { }

private data class PosixEventGroups(
    val pollable: List<PollableSubscription>,
    val nonPollable: List<NonPollableSubscription>,
    val events: List<Event>,
)

private data class PollableSubscription(
    val fd: NativeFileFd,
    val needClose: Boolean,
    val subscription: Subscription,
)

private data class NonPollableSubscription(
    val subscription: FileDescriptorSubscription,
    val resource: FdResource,
)

private fun createTimerSubscription(
    subscription: ClockSubscription,
): Either<FileSystemErrno, PollableSubscription> = either {
    val clockId = subscription.clock.posixClockId
    if (clockId != CLOCK_REALTIME && clockId != CLOCK_MONOTONIC) {
        raise(NOTSUP)
    }

    val timerFd = linuxTimerfdCreate(subscription.clock)
        .mapLeft(LinuxTimerfdCreateError::errno)
        .bind()

    linuxTimerfdSetTime(
        timerFd,
        subscription.timeout.timeoutNs,
        subscription.timeout is SubscriptionTimeout.Absolute,
    )
        .onLeft { error ->
            posixClose(timerFd).onLeft { /* ignore */ }
        }
        .mapLeft(LinuxSettimeError::errno)
        .bind()

    PollableSubscription(
        fd = timerFd,
        needClose = true,
        subscription = subscription,
    )
}

private fun nanosleep(
    clock: SubscriptionClockId,
    timeoutNs: Long,
): Either<FileSystemErrno, Unit> {
    return memScoped {
        @Suppress("MagicNumber")
        val timespec: timespec = alloc<timespec>().apply {
            tv_sec = timeoutNs / 1_000_000_000L
            tv_nsec = (timeoutNs % 1_000_000_000)
        }
        val result = clock_nanosleep(clock.posixClockId, 0, timespec.ptr, null)
        when (result) {
            0 -> Unit.right()
            EINTR -> FileSystemErrno.INTR.left()
            EINVAL -> FileSystemErrno.INVAL.left()
            ENOTSUP -> NOTSUP.left()
            else -> FileSystemErrno.INVAL.left()
        }
    }
}
