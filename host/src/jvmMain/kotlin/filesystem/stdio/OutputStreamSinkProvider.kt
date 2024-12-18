/*
 * Copyright 2024, the wasi-emscripten-host project authors and contributors. Please see the AUTHORS file
 * for details. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 * SPDX-License-Identifier: Apache-2.0
 */

package at.released.weh.filesystem.stdio

import kotlinx.io.RawSink
import kotlinx.io.asSink
import java.io.OutputStream

internal class OutputStreamSinkProvider(
    private val streamProvider: () -> OutputStream,
) : SinkProvider {
    override fun open(): RawSink = streamProvider().asSink()
}