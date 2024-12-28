/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.nio

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
import at.released.weh.host.clock.MonotonicClock
import at.released.weh.host.jvm.clock.JvmMonotonicClock
import kotlin.concurrent.withLock

internal class NioPoll(
    private val fsState: NioFileSystemState,
    private val monotonicClock: MonotonicClock = JvmMonotonicClock,
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

@Suppress("MagicNumber", "UnusedParameter")
private fun nanosleep(
    clock: Subscription.SubscriptionClockId,
    timeoutNs: Long,
): Either<FileSystemErrno, Unit> {
    try {
        Thread.sleep(timeoutNs / 1_000_000, (timeoutNs % 1_000_000).toInt())
    } catch (_: InterruptedException) {
        return FileSystemErrno.INTR.left()
    }
    return Unit.right()
}
