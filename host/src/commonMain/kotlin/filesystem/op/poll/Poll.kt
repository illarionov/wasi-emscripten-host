/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.op.poll

import at.released.weh.filesystem.error.PollError
import at.released.weh.filesystem.op.FileSystemOperation

public data class Poll(
    val subscriptions: List<Subscription>,
) {
    public companion object : FileSystemOperation<Poll, PollError, List<Event>> {
        override val tag: String = "poll"
    }
}
