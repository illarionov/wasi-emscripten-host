/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("MagicNumber")

package at.released.weh.wasi.preview1.ext

import at.released.weh.filesystem.op.poll.FileDescriptorEventType
import at.released.weh.filesystem.op.poll.Subscription.ClockSubscription
import at.released.weh.filesystem.op.poll.Subscription.FileDescriptorSubscription
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionClockId
import at.released.weh.filesystem.op.poll.Subscription.SubscriptionTimeout
import at.released.weh.wasi.preview1.type.Clockid
import at.released.weh.wasi.preview1.type.Eventtype
import at.released.weh.wasi.preview1.type.SubclockflagsFlag.SUBSCRIPTION_CLOCK_ABSTIME
import at.released.weh.wasi.preview1.type.Subscription
import at.released.weh.wasi.preview1.type.SubscriptionClock
import at.released.weh.wasi.preview1.type.SubscriptionFdReadwrite
import at.released.weh.wasi.preview1.type.SubscriptionU
import kotlinx.io.Source
import kotlinx.io.readIntLe
import kotlinx.io.readLongLe
import kotlinx.io.readShortLe
import kotlin.experimental.and
import at.released.weh.filesystem.op.poll.Subscription as FileSystemSubscription

internal object SubscriptionMapper {
    internal const val SUBSCRIPTION_SIZE = 48L
    private const val SUBSCRIPTION_U_SIZE = 40L
    private const val SUBSCRIPTION_CLOCK_SIZE = 32L
    private const val SUBSCRIPTION_FD_READWRITE_SIZE = 4L

    internal fun readSubscriptions(
        source: Source,
        count: Int,
    ): List<Subscription> {
        source.require(SUBSCRIPTION_SIZE * count)
        return MutableList(count) {
            readSubscription(source)
        }
    }

    internal fun readSubscription(
        source: Source,
    ): Subscription {
        source.require(SUBSCRIPTION_SIZE)
        return Subscription(
            userdata = source.readLongLe(),
            u = readSubscriptionU(source),
        )
    }

    private fun readSubscriptionU(
        source: Source,
    ): SubscriptionU {
        source.require(SUBSCRIPTION_U_SIZE)
        val tag: Eventtype = source.readByte().toInt().let(Eventtype::fromCode) ?: error("Incorrect Eventtype")
        source.skip(7) // alignment
        return when (tag) {
            Eventtype.CLOCK -> readSubscriptionClock(source)
            Eventtype.FD_READ -> readSubscriptionReadWrite(source, tag).also {
                source.skip(28) // alignment
            }

            Eventtype.FD_WRITE -> readSubscriptionReadWrite(source, tag).also {
                source.skip(28) // alignment
            }
        }
    }

    private fun readSubscriptionClock(
        source: Source,
    ): SubscriptionClock {
        source.require(SUBSCRIPTION_CLOCK_SIZE)
        val clockId = Clockid.fromCode(source.readIntLe()) ?: error("Incorrect clockId")
        source.skip(4) // alignment
        val clock = SubscriptionClock(
            tag = Eventtype.CLOCK,
            id = clockId,
            timeout = source.readLongLe(),
            precision = source.readLongLe(),
            flags = source.readShortLe(),
        )
        source.skip(6) // alignment
        return clock
    }

    private fun readSubscriptionReadWrite(
        source: Source,
        type: Eventtype,
    ): SubscriptionFdReadwrite {
        source.require(SUBSCRIPTION_FD_READWRITE_SIZE)
        return SubscriptionFdReadwrite(
            tag = type,
            fileDescriptor = source.readIntLe(),
        )
    }

    internal fun toFileSystemSubscription(subscription: Subscription): FileSystemSubscription {
        return when (val subscriptionU = subscription.u) {
            is SubscriptionClock -> ClockSubscription(
                userdata = subscription.userdata,
                clock = subscriptionU.id.toSubscriptionClockId(),
                timeout = if (subscriptionU.flags and SUBSCRIPTION_CLOCK_ABSTIME == SUBSCRIPTION_CLOCK_ABSTIME) {
                    SubscriptionTimeout.Absolute(subscriptionU.timeout, subscriptionU.precision)
                } else {
                    SubscriptionTimeout.Relative(subscriptionU.timeout, subscriptionU.precision)
                },
            )

            is SubscriptionFdReadwrite -> FileDescriptorSubscription(
                userdata = subscription.userdata,
                fileDescriptor = subscriptionU.fileDescriptor,
                type = when (subscriptionU.tag) {
                    Eventtype.CLOCK -> error("Incorrect Eventtype")
                    Eventtype.FD_READ -> FileDescriptorEventType.READ
                    Eventtype.FD_WRITE -> FileDescriptorEventType.WRITE
                },
            )
        }
    }

    private fun Clockid.toSubscriptionClockId(): SubscriptionClockId = when (this) {
        Clockid.REALTIME -> SubscriptionClockId.REALTIME
        Clockid.MONOTONIC -> SubscriptionClockId.MONOTONIC
        Clockid.PROCESS_CPUTIME_ID -> SubscriptionClockId.PROCESS_CPUTIME_ID
        Clockid.THREAD_CPUTIME_ID -> SubscriptionClockId.THREAD_CPUTIME_ID
    }
}
