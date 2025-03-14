/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.windows

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import at.released.weh.filesystem.error.InvalidArgument
import at.released.weh.filesystem.error.PollError
import at.released.weh.filesystem.internal.delegatefs.FileSystemOperationHandler
import at.released.weh.filesystem.internal.op.poll.PollHelper
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.op.poll.Event
import at.released.weh.filesystem.op.poll.Poll
import at.released.weh.filesystem.op.poll.Subscription
import at.released.weh.filesystem.posix.op.poll.posixClockId
import at.released.weh.filesystem.windows.fdresource.WindowsFileSystemState
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.windows.clock.WindowsMonotonicClock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.EINTR
import platform.posix.EINVAL
import platform.posix.ENOTSUP
import platform.posix.clock_nanosleep
import platform.posix.timespec

internal class WindowsPoll(
    private val fsState: WindowsFileSystemState,
    private val monotonicClock: MonotonicClock = WindowsMonotonicClock,
) : FileSystemOperationHandler<Poll, PollError, List<Event>> {
    private val pollHelper = PollHelper(
        fdsResourceProvider = { fsState.get(it) },
        monotonicClock = monotonicClock,
        nanosleep = ::nanosleep,
    )

    override fun invoke(input: Poll): Either<PollError, List<Event>> = either {
        val subscriptionGroups = fsState.fdsLock.withLock {
            pollHelper.groupSubscriptions(input.subscriptions)
        }
        if (subscriptionGroups.events.isNotEmpty()) {
            return subscriptionGroups.events.right()
        }

        return if (subscriptionGroups.fdsToBlock.isEmpty() && subscriptionGroups.clockSubscriptions.isEmpty()) {
            InvalidArgument("Subscription list is empty").left()
        } else if (subscriptionGroups.fdsToBlock.isEmpty()) {
            pollHelper.waitNearestTimer(subscriptionGroups.clockSubscriptions)
        } else {
            pollHelper.pollSubscriptions(subscriptionGroups.fdsToBlock, subscriptionGroups.clockSubscriptions)
        }
    }
}

private fun nanosleep(
    clock: Subscription.SubscriptionClockId,
    timeoutNs: Long,
): Either<FileSystemErrno, Unit> {
    return memScoped {
        @Suppress("MagicNumber")
        val timespec: timespec = alloc<timespec>().apply {
            tv_sec = timeoutNs / 1_000_000_000L
            tv_nsec = (timeoutNs % 1_000_000_000).toInt()
        }
        val result = clock_nanosleep(clock.posixClockId, 0, timespec.ptr, null)
        when (result) {
            0 -> Unit.right()
            EINTR -> FileSystemErrno.INTR.left()
            EINVAL -> FileSystemErrno.INVAL.left()
            ENOTSUP -> FileSystemErrno.NOTSUP.left()
            else -> FileSystemErrno.INVAL.left()
        }
    }
}
