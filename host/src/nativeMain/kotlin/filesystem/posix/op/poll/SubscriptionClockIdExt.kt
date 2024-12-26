/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.posix.op.poll

import at.released.weh.filesystem.op.poll.Subscription
import platform.posix.CLOCK_MONOTONIC
import platform.posix.CLOCK_PROCESS_CPUTIME_ID
import platform.posix.CLOCK_REALTIME
import platform.posix.CLOCK_THREAD_CPUTIME_ID

internal val Subscription.SubscriptionClockId.posixClockId: Int get() = when (this) {
    Subscription.SubscriptionClockId.REALTIME -> CLOCK_REALTIME
    Subscription.SubscriptionClockId.MONOTONIC -> CLOCK_MONOTONIC
    Subscription.SubscriptionClockId.PROCESS_CPUTIME_ID -> CLOCK_PROCESS_CPUTIME_ID
    Subscription.SubscriptionClockId.THREAD_CPUTIME_ID -> CLOCK_THREAD_CPUTIME_ID
}
