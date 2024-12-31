/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")

package at.released.weh.filesystem.apple

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.PollError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.poll.getMinimalTimeoutOrNull
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.BADF
import at.released.weh.filesystem.op.poll.Event
import at.released.weh.filesystem.op.poll.Poll
import at.released.weh.filesystem.op.poll.Subscription
import at.released.weh.filesystem.op.poll.Subscription.ClockSubscription
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.poll.toEvent
import at.released.weh.filesystem.posix.NativeFileFd
import at.released.weh.filesystem.posix.fdresource.ResourceWithPollableFileDescriptor
import at.released.weh.filesystem.posix.poll.PosixPollHelper
import at.released.weh.filesystem.posix.poll.PosixPollHelper.NonPollableSubscription
import at.released.weh.filesystem.posix.poll.PosixPollHelper.PollableSubscription
import at.released.weh.host.apple.clock.AppleClock
import at.released.weh.host.apple.clock.AppleMonotonicClock
import at.released.weh.host.clock.Clock
import at.released.weh.host.clock.MonotonicClock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.ENOTSUP
import platform.posix.timespec
import platform.posix.nanosleep as posixNanosleep

internal class ApplePoll(
    private val fsState: AppleFileSystemState,
    private val realtimeClock: Clock = AppleClock,
    private val monotonicClock: MonotonicClock = AppleMonotonicClock,
) : FileSystemOperationHandler<Poll, PollError, List<Event>> {
    private val pollHelper = PosixPollHelper(
        monotonicClock = monotonicClock,
        nanosleep = ::nanosleep,
    )

    @Suppress("DestructuringDeclarationWithTooManyEntries")
    override fun invoke(input: Poll): Either<PollError, List<Event>> = either {
        val (pollable, nonPollable, clockSubscriptions, events) = fsState.fdsLock.withLock {
            groupSubscriptions(input.subscriptions)
        }

        return try {
            if (events.isNotEmpty()) {
                return events.right()
            }
            if (pollable.isEmpty() && nonPollable.isEmpty() && clockSubscriptions.isEmpty()) {
                return InvalidArgument("Subscription list is empty").left()
            }
            val timeoutSubscription = getMinimalTimeoutOrNull(clockSubscriptions, realtimeClock, monotonicClock)
            pollHelper.pollSubscriptions(pollable, nonPollable, timeoutSubscription)
        } finally {
            pollHelper.closeSubscriptions(pollable)
        }
    }

    private fun groupSubscriptions(
        subscriptions: List<Subscription>,
    ): AppleEventGroups {
        val pollable: MutableList<PollableSubscription> = ArrayList(subscriptions.size)
        val nonPollable: MutableList<NonPollableSubscription> = mutableListOf()
        val clockSubscriptions: MutableList<ClockSubscription> = mutableListOf()
        val events: MutableList<Event> = ArrayList(subscriptions.size)

        subscriptions.forEach { subscription: Subscription ->
            when (subscription) {
                is ClockSubscription -> clockSubscriptions.add(subscription)
                is FileDescriptorSubscription -> when (val channel = fsState.get(subscription.fileDescriptor)) {
                    null -> events.add(subscription.toEvent(errno = BADF))
                    is ResourceWithPollableFileDescriptor -> channel.getPollableFileDescriptor(subscription.type)
                        .onLeft { nonPollable.add(NonPollableSubscription(subscription, channel)) }
                        .onRight { pollable.add(PollableSubscription(NativeFileFd(it), false, subscription)) }
                    else -> nonPollable.add(NonPollableSubscription(subscription, channel))
                }
            }
        }
        return AppleEventGroups(pollable, nonPollable, clockSubscriptions, events)
    }
}

private data class AppleEventGroups(
    val pollableSubscriptions: List<PollableSubscription>,
    val nonPollableSubscriptions: List<NonPollableSubscription>,
    val clockSubscriptions: List<ClockSubscription>,
    val events: List<Event>,
)

private fun nanosleep(
    @Suppress("UnusedParameter") clock: Subscription.SubscriptionClockId,
    timeoutNs: Long,
): Either<FileSystemErrno, Unit> {
    return memScoped {
        @Suppress("MagicNumber")
        val timespec: timespec = alloc<timespec>().apply {
            tv_sec = timeoutNs / 1_000_000_000L
            tv_nsec = (timeoutNs % 1_000_000_000)
        }
        val result = posixNanosleep(timespec.ptr, null)
        when (result) {
            0 -> Unit.right()
            EINTR -> FileSystemErrno.INTR.left()
            EINVAL -> FileSystemErrno.INVAL.left()
            ENOTSUP -> FileSystemErrno.NOTSUP.left()
            else -> FileSystemErrno.INVAL.left()
        }
    }
}
