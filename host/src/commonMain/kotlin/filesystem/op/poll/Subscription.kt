/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.poll

import at.released.weh.filesystem.model.FileDescriptor
import at.released.weh.filesystem.model.FileSystemErrno
import at.released.weh.filesystem.model.FileSystemErrno.SUCCESS
import at.released.weh.filesystem.op.poll.Event.ClockEvent
import at.released.weh.filesystem.op.poll.Subscription.ClockSubscription
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription

public sealed interface Subscription {
    public val userdata: Long

    public sealed interface SubscriptionTimeout {
        public val timeoutNs: Long
        public val precisionNs: Long

        public data class Absolute(
            override val timeoutNs: Long,
            override val precisionNs: Long,
        ) : SubscriptionTimeout

        public data class Relative(
            override val timeoutNs: Long,
            override val precisionNs: Long,
        ) : SubscriptionTimeout
    }

    public enum class SubscriptionClockId {
        REALTIME,
        MONOTONIC,
        PROCESS_CPUTIME_ID,
        THREAD_CPUTIME_ID,
    }

    public data class ClockSubscription(
        override val userdata: Long = 0,
        val clock: SubscriptionClockId,
        val timeout: SubscriptionTimeout,
    ) : Subscription

    public data class FileDescriptorSubscription(
        override val userdata: Long = 0,
        public val fileDescriptor: FileDescriptor,
        val type: FileDescriptorEventType,
    ) : Subscription
}

internal fun ClockSubscription.toEvent(
    errno: FileSystemErrno = SUCCESS,
) = ClockEvent(
    errno = errno,
    userdata = this.userdata,
)

internal fun FileDescriptorSubscription.toEvent(
    errno: FileSystemErrno = SUCCESS,
    bytesAvailable: Long = 0,
    isHangup: Boolean = false,
) = Event.FileDescriptorEvent(
    errno = errno,
    userdata = userdata,
    fileDescriptor = fileDescriptor,
    type = type,
    bytesAvailable = bytesAvailable,
    isHangup = isHangup,
)
