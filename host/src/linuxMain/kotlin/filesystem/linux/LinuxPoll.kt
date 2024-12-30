/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")

package at.released.weh.filesystem.linux

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.PollError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.poll.getMinimalTimeoutOrNull
import at.released.weh.filesystem.linux.fdresource.LinuxFileSystemState
import at.released.weh.filesystem.linux.native.LinuxSettimeError
import at.released.weh.filesystem.linux.native.LinuxTimerfdCreateError
import at.released.weh.filesystem.linux.native.linuxTimerfdCreate
import at.released.weh.filesystem.linux.native.linuxTimerfdSetTime
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.model.FileSystemErrno.NOTSUP
import at.released.weh.filesystem.op.poll.Event
import at.released.weh.filesystem.op.poll.Event.ClockEvent
import at.released.weh.filesystem.op.poll.Poll
import at.released.weh.filesystem.op.poll.Subscription
import at.released.weh.filesystem.op.poll.Subscription.ClockSubscription
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionClockId
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionTimeout
import at.released.weh.filesystem.op.poll.toEvent
import at.released.weh.filesystem.posix.nativefunc.posixClose
import at.released.weh.filesystem.posix.op.poll.posixClockId
import at.released.weh.filesystem.posix.poll.PosixPollHelper
import at.released.weh.filesystem.posix.poll.PosixPollHelper.NonPollableSubscription
import at.released.weh.filesystem.posix.poll.PosixPollHelper.PollableSubscription
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.linux.clock.LinuxClock
import at.released.weh.host.linux.clock.LinuxMonotonicClock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.CLOCK_MONOTONIC
import platform.posix.CLOCK_REALTIME
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.ENOTSUP
import platform.posix.clock_nanosleep
import platform.posix.timespec

internal class LinuxPoll(
    private val fsState: LinuxFileSystemState,
    private val realtimeClock: Clock = LinuxClock,
    private val monotonicClock: MonotonicClock = LinuxMonotonicClock,
) : FileSystemOperationHandler<Poll, PollError, List<Event>> {
    private val pollHelper = PosixPollHelper(
        monotonicClock = monotonicClock,
        nanosleep = ::nanosleep,
    )

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
            pollHelper.pollSubscriptions(pollable, nonPollable, timeoutSubscription)
        } finally {
            pollHelper.closeSubscriptions(pollable)
        }
    }

    private fun groupSubscriptions(
        subscriptions: List<Subscription>,
    ): LinuxEventGroups {
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
                        events.add(subscription.toEvent(errno = BADF))
                    }
                }
            }
        }
        return LinuxEventGroups(pollable, nonPollable, events)
    }
}

private fun createTimerSubscription(
    clockSubscription: ClockSubscription,
): Either<FileSystemErrno, PollableSubscription> = either {
    val clockId = clockSubscription.clock.posixClockId
    if (clockId != CLOCK_REALTIME && clockId != CLOCK_MONOTONIC) {
        raise(NOTSUP)
    }

    val timerFd = linuxTimerfdCreate(clockSubscription.clock)
        .mapLeft(LinuxTimerfdCreateError::errno)
        .bind()

    linuxTimerfdSetTime(
        timerFd,
        clockSubscription.timeout.timeoutNs,
        clockSubscription.timeout is SubscriptionTimeout.Absolute,
    )
        .onLeft {
            posixClose(timerFd).onLeft { /* ignore */ }
        }
        .mapLeft(LinuxSettimeError::errno)
        .bind()

    PollableSubscription(
        fd = timerFd,
        needClose = true,
        subscription = clockSubscription,
    )
}

private data class LinuxEventGroups(
    val pollableSubscriptions: List<PollableSubscription>,
    val nonPollableSubscriptions: List<NonPollableSubscription>,
    val events: List<Event>,
)

private fun nanosleep(
    clock: SubscriptionClockId,
    timeoutNs: Long,
): Either<FileSystemErrno, Unit> = memScoped {
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
