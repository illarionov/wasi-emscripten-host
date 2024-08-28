/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("OPT_IN_USAGE")

package at.released.weh.test.utils

import kotlin.native.concurrent.Worker

@OptIn(ExperimentalStdlibApi::class)
internal actual val currentThreadId: ULong get() = Worker.current.platformThreadId
internal actual val currentTimestamp: ULong get() = 0UL
